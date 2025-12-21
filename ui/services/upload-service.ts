import { addUpload, getAllUploads, updateUpload } from '@/lib/db';
import { API_BASE_URL } from '@/lib/constants';
import Cookies from 'js-cookie';
import { toast } from 'sonner';

export interface UploadTask {
    uploadId: string;
    file: File;
    chunkSize: number;
    progress: number;
    status: 'PENDING' | 'UPLOADING' | 'PAUSED' | 'COMPLETED' | 'ERROR';
    folderId?: string | null;
}

type UploadListener = (uploads: UploadTask[]) => void;

class UploadService {
    private readonly uploads: Map<string, UploadTask> = new Map();
    private activeUploads: number = 0;
    private listeners: UploadListener[] = [];
    private readonly MAX_CONCURRENT_UPLOADS = 3;
    private readonly MAX_CONCURRENT_CHUNKS = 3;

    constructor() {
        // Hydrate from DB on init
        if (typeof globalThis.window !== 'undefined') {
            this.hydrate().catch(console.error);
        }
    }

    private async hydrate() {
        try {
            const storedUploads = await getAllUploads();
            storedUploads.forEach(u => {
                this.uploads.set(u.uploadId, { ...u, folderId: undefined }); // DB schema might differ slightly, adapting
            });
            this.notifyListeners();
        } catch (e) {
            console.error("Failed to hydrate uploads", e);
        }
    }

    public subscribe(listener: UploadListener) {
        this.listeners.push(listener);
        listener(Array.from(this.uploads.values()));
        return () => {
            this.listeners = this.listeners.filter(l => l !== listener);
        };
    }

    private notifyListeners() {
        const tasks = Array.from(this.uploads.values());
        this.listeners.forEach(l => l(tasks));
    }

    async initiateUpload(file: File, folderId?: string) {
        // 1. Init API Call
        const token = Cookies.get('token');
        try {
            const res = await fetch(`${API_BASE_URL}/upload/init`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? `Bearer ${token}` : ''
                },
                body: JSON.stringify({
                    fileName: file.name,
                    fileSize: file.size,
                    mimeType: file.type || 'application/octet-stream',
                    parentId: folderId
                })
            });

            if (!res.ok) throw new Error("Failed to initiate upload");

            const data = await res.json();
            const uploadId = data.uploadId;
            // Assuming backend returns chunkSize, if not default to 10MB
            const chunkSize = data.chunkSize || 10 * 1024 * 1024;

            const task: UploadTask = {
                uploadId,
                file,
                chunkSize,
                progress: 0,
                status: 'PENDING',
                folderId
            };

            this.uploads.set(uploadId, task);
            await addUpload({ ...task, createdAt: Date.now() });
            this.notifyListeners();

            this.processQueue();

        } catch (e) {
            console.error("Upload init error", e);
            toast.error("Failed to start upload: " + file.name);
        }
    }

    async resumeUpload(uploadId: string) {
        const task = this.uploads.get(uploadId);
        if (task && (task.status === 'PAUSED' || task.status === 'ERROR')) {
            task.status = 'PENDING';
            this.uploads.set(uploadId, task);
            await updateUpload({ ...task, createdAt: Date.now() });
            this.notifyListeners();
            this.processQueue();
        }
    }

    async pauseUpload(uploadId: string) {
        const task = this.uploads.get(uploadId);
        if (task?.status === 'UPLOADING') {
            task.status = 'PAUSED';
            this.uploads.set(uploadId, task);
            await updateUpload({ ...task, createdAt: Date.now() });
            this.notifyListeners();
            // Actual cancellation of fetches logic would go here (using AbortController usually)
        }
    }

    private async processQueue() {
        if (this.activeUploads >= this.MAX_CONCURRENT_UPLOADS) return;

        const nextTask = Array.from(this.uploads.values()).find(t => t.status === 'PENDING');
        if (!nextTask) return;

        this.activeUploads++;
        nextTask.status = 'UPLOADING';
        this.uploads.set(nextTask.uploadId, nextTask);
        this.notifyListeners();

        try {
            await this.uploadFile(nextTask);
            nextTask.status = 'COMPLETED';
            toast.success("Upload completed: " + nextTask.file.name);
        } catch (e) {
            console.error("Upload failed", e);
            nextTask.status = 'ERROR';
            toast.error("Upload failed: " + nextTask.file.name);
        } finally {
            this.activeUploads--;
            this.uploads.set(nextTask.uploadId, nextTask);
            await updateUpload({ ...nextTask, createdAt: Date.now() }); // Update final state DB
            this.notifyListeners();
            this.processQueue();
        }
    }

    private async uploadFile(task: UploadTask) {
        const totalChunks = Math.ceil(task.file.size / task.chunkSize);

        let activeChunkUploads = 0;
        let nextChunkIndex = 0;
        let completedChunksCount = 0;
        let error: any = null;

        return new Promise<void>((resolve, reject) => {
            const next = async () => {
                if (error) return;
                if (this.uploads.get(task.uploadId)?.status === 'PAUSED') return;

                if (completedChunksCount === totalChunks) {
                    try {
                        await this.completeUpload(task.uploadId);
                        resolve();
                    } catch (e) {
                        reject(e);
                    }
                    return;
                }

                while (activeChunkUploads < this.MAX_CONCURRENT_CHUNKS && nextChunkIndex < totalChunks) {
                    if (this.uploads.get(task.uploadId)?.status === 'PAUSED') return;

                    const chunkIndex = nextChunkIndex++;
                    activeChunkUploads++;

                    const start = chunkIndex * task.chunkSize;
                    const end = Math.min(start + task.chunkSize, task.file.size);
                    const chunk = task.file.slice(start, end);

                    this.uploadChunk(task.uploadId, chunkIndex, chunk)
                        .then(async () => {
                            activeChunkUploads--;
                            completedChunksCount++;

                            task.progress = Math.round((completedChunksCount / totalChunks) * 100);

                            // Update memory state always
                            this.uploads.set(task.uploadId, task);
                            this.notifyListeners();

                            // Debounce DB updates
                            if (completedChunksCount % 3 === 0 || completedChunksCount === totalChunks) {
                                await updateUpload({ ...task, createdAt: Date.now() });
                            }

                            next();
                        })
                        .catch((e) => {
                            activeChunkUploads--;
                            error = e;
                            reject(e);
                        });
                }
            };
            next();
        });
    }


    private async uploadChunk(uploadId: string, index: number, chunk: Blob) {
        const token = Cookies.get('token');
        const res = await fetch(`${API_BASE_URL}/upload/chunk/${uploadId}/${index}`, {
            method: 'POST',
            headers: {
                'Authorization': token ? `Bearer ${token}` : '',
                'Content-Type': 'application/octet-stream' // Binary stream
            },
            body: chunk
        });
        if (!res.ok) throw new Error(`Failed chunk ${index}`);
    }

    private async completeUpload(uploadId: string) {
        const token = Cookies.get('token');
        const res = await fetch(`${API_BASE_URL}/upload/complete/${uploadId}`, {
            method: 'GET', // Or POST based on your API
            headers: {
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });
        if (!res.ok) throw new Error("Failed to complete upload");
    }

    public get upload() {
        return [...this.uploads.values()]
    }
}

export const uploadService = new UploadService();
