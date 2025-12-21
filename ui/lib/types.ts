export interface ItemDto {
    id: string;
    name: string;
    type: 'FILE' | 'FOLDER';
    parentId?: string;
    size?: number;
    mimeType?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface UploadState {
    progress: number;
    status: 'PENDING' | 'UPLOADING' | 'PAUSED' | 'COMPLETED' | 'ERROR';
    error?: string;
}

// Unified interface for UI
export interface FileItem {
    id: string; // db id or uploadId
    name: string;
    type: 'FILE' | 'FOLDER';
    size: number;
    mimeType?: string;
    updatedAt?: number | string; // timestamp or ISO string

    // UI State
    isUploading?: boolean;
    uploadProgress?: number;
    uploadStatus?: 'PENDING' | 'UPLOADING' | 'PAUSED' | 'COMPLETED' | 'ERROR';
}
