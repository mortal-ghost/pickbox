"use client";

import { useAuth } from "@/context/auth-context";
import { Navbar } from "@/components/landing/navbar";

import { SearchBar } from "./search-bar";
import { UploadFab } from "@/components/home/upload-fab";
import { LayoutGrid, List, Loader2 } from "lucide-react";
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { uploadService, UploadTask } from "@/services/upload-service";
import { fileService } from "@/services/file-service";

import { FileCard } from "@/components/home/file-card";
import { FileRow } from "@/components/home/file-row";
import { FileItem } from "@/lib/types";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";

export default function HomePage() {
    const { user } = useAuth();
    const [searchQuery, setSearchQuery] = useState("");
    const [currentFolderId, setCurrentFolderId] = useState<string | null>(null);
    const [currentFolderName, setCurrentFolderName] = useState<string | null>(null);
    const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
    const [uploadTasks, setUploadTasks] = useState<UploadTask[]>([]);

    // Folder Creation State
    const [isCreateFolderOpen, setIsCreateFolderOpen] = useState(false);
    const [newFolderName, setNewFolderName] = useState("New Folder");

    const [serverFiles, setServerFiles] = useState<FileItem[]>([]);
    const [isLoading, setIsLoading] = useState(false);

    const refreshFiles = async () => {
        try {
            setIsLoading(true);
            const files = await fileService.listFiles(currentFolderId || undefined);

            const mappedFiles: FileItem[] = files.map(f => ({
                id: f.id,
                name: f.name,
                type: f.type,
                size: f.size || 0,
                updatedAt: f.updatedAt || Date.now()
            }));
            setServerFiles(mappedFiles);
        } catch (error) {
            console.error("Failed to load files", error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        refreshFiles();
    }, [currentFolderId]);

    useEffect(() => {
        // Initial load
        setUploadTasks(uploadService.upload || []);

        const unsubscribe = uploadService.subscribe((uploads) => {
            setUploadTasks(uploads);
        });
        return unsubscribe;
    }, []);

    const handleSearch = (query: string) => {
        setSearchQuery(query);
    };

    const handleDownload = (item: FileItem) => {
        console.log("Download", item);
    };

    const handleCreateFolder = async () => {
        const folderName = newFolderName.trim() || "New Folder";
        setIsCreateFolderOpen(false); // Close dialog
        setNewFolderName("New Folder"); // Reset

        const tempId = "temp-" + Date.now();

        const optimisticFolder: FileItem = {
            id: tempId,
            name: folderName,
            type: 'FOLDER',
            size: 0,
            updatedAt: Date.now(),
            isUploading: true,
            uploadStatus: 'PENDING',
            uploadProgress: 0
        };

        setServerFiles(prev => [optimisticFolder, ...prev]);

        try {
            const newFolder = await fileService.createFolder(folderName, currentFolderId);
            setServerFiles(prev => prev.map(item =>
                item.id === tempId ? {
                    ...item,
                    id: newFolder.id,
                    name: newFolder.name,
                    isUploading: false,
                    updatedAt: newFolder.updatedAt || Date.now()
                } : item
            ));
        } catch (e) {
            console.error(e);
            setServerFiles(prev => prev.filter(item => item.id !== tempId));
            toast.error("Failed to create folder");
        }
    };

    const handleFolderClick = (item: FileItem) => {
        if (item.type === 'FOLDER' && !item.isUploading) {
            setCurrentFolderId(item.id);
            setCurrentFolderName(item.name);
        }
    };

    const handleGoBack = () => {
        // Simple go back for now, ideally we need breadcrumbs or parentId tracking from backend
        // Since we don't have parentId history in state yet easily without fetching,
        // we'll just go to root for now or need a better strategy. 
        // For this step, let's just allow going to root if not root.
        setCurrentFolderId(null);
        setCurrentFolderName(null);
    };

    // Combine Server + Uploads
    const displayedItems: FileItem[] = [
        ...uploadTasks
            .filter(t => t.status !== 'COMPLETED')
            .map(t => ({
                id: t.uploadId,
                name: t.file.name,
                type: 'FILE' as const, // Explicit const assertion
                size: t.file.size,
                updatedAt: Date.now(),
                isUploading: t.status !== 'COMPLETED',
                uploadProgress: t.progress,
                uploadStatus: t.status
            })),
        ...serverFiles
    ];


    return (
        <div className="flex h-screen flex-col bg-background overflow-hidden">
            <Navbar />

            <div className="my-8 mx-4 shrink-0">
                <h1 className="text-3xl font-bold">Dashboard</h1>
            </div>

            <main className="container mx-auto px-4 flex-1 flex flex-col gap-6 overflow-hidden pb-4">
                <div className="flex justify-between items-center gap-4 shrink-0">
                    <SearchBar onSearch={handleSearch} />
                    <div className="flex items-center gap-2 border rounded-md p-1 bg-muted/20 shrink-0">
                        <Button
                            variant={viewMode === "grid" ? "secondary" : "ghost"}
                            size="icon"
                            className="h-8 w-8"
                            onClick={() => setViewMode("grid")}
                        >
                            <LayoutGrid className="h-4 w-4" />
                        </Button>
                        <Button
                            variant={viewMode === "list" ? "secondary" : "ghost"}
                            size="icon"
                            className="h-8 w-8"
                            onClick={() => setViewMode("list")}
                        >
                            <List className="h-4 w-4" />
                        </Button>
                    </div>
                </div>

                <div className="flex-1 rounded-lg border bg-card text-card-foreground shadow-sm overflow-y-auto p-6">
                    <h3 className="font-semibold leading-none tracking-tight mb-2 sticky top-0 bg-card z-10 pb-2 border-b">My Files</h3>
                    <div className="mt-4">
                        <div className="flex items-center gap-2 mb-4">
                            {currentFolderId && (
                                <Button variant="outline" size="sm" onClick={handleGoBack}>
                                    ← Home
                                </Button>
                            )}
                            <p className="text-sm text-muted-foreground">
                                {currentFolderId ? `Current Folder: ${currentFolderName || currentFolderId}` : "Root Directory"}
                            </p>
                        </div>

                        <p className="text-sm text-muted-foreground mb-4">
                            {searchQuery ? `Searching for: "${searchQuery}"` : `You have ${displayedItems.length} items.`}
                        </p>

                        {isLoading ? (
                            <div className="flex justify-center items-center py-20 text-muted-foreground">
                                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground/50" />
                            </div>
                        ) : displayedItems.length === 0 && !searchQuery ? (
                            <div className="text-center py-20 text-muted-foreground">
                                <p>No files yet.</p>
                                <p className="text-xs mt-2">Upload a file to get started.</p>
                            </div>
                        ) : (
                            <div className={viewMode === 'grid' ? "grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4" : "flex flex-col gap-2"}>
                                {displayedItems.map(item => (
                                    viewMode === 'grid' ? (
                                        <FileCard
                                            key={item.id}
                                            item={item}
                                            onDownload={handleDownload}
                                            onFolderClick={handleFolderClick}
                                        />
                                    ) : (
                                        <FileRow
                                            key={item.id}
                                            item={item}
                                            onDownload={handleDownload}
                                            onFolderClick={handleFolderClick}
                                        />
                                    )
                                ))}
                            </div>
                        )}

                        {/* Fallback scroll test 
                        <div className="mt-8 pt-8 border-t">
                            <p className="text-xs text-muted-foreground">Scroll Test</p>
                             {Array.from({ length: 5 }).map((_, i) => (
                                <div key={i} className="py-2 border-b last:border-0 border-border/40 text-sm text-muted-foreground">
                                    Placeholder File Item {i + 1}
                                </div>
                            ))}
                        </div>
                        */}
                    </div>
                </div>
            </main>
            <UploadFab
                onFileUpload={(files) => {
                    Array.from(files).forEach(file => {
                        uploadService.initiateUpload(file, currentFolderId || undefined);
                    });
                }}
                onCreateFolder={() => {
                    setIsCreateFolderOpen(true);
                }}
            />

            <Dialog open={isCreateFolderOpen} onOpenChange={setIsCreateFolderOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Create New Folder</DialogTitle>
                        <DialogDescription>
                            Enter a name for the new folder.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="name" className="text-right">
                                Name
                            </Label>
                            <Input
                                id="name"
                                value={newFolderName}
                                onChange={(e) => setNewFolderName(e.target.value)}
                                className="col-span-3"
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter') handleCreateFolder();
                                }}
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button type="submit" onClick={handleCreateFolder}>Create Folder</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
