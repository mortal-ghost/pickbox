"use client";

import { FileItem } from "@/lib/types";
import { File as FileIcon, Folder, MoreVertical, Download, Info, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface FileCardProps {
    readonly item: FileItem;
    readonly onDownload?: (item: FileItem) => void;
    readonly onOptions?: (item: FileItem) => void;
    readonly onFolderClick?: (item: FileItem) => void;
    readonly onPause?: (item: FileItem) => void;
    readonly onResume?: (item: FileItem) => void;
    readonly onCancel?: (item: FileItem) => void;
}

export function FileCard({ item, onDownload, onOptions, onFolderClick, onPause, onResume, onCancel }: FileCardProps) {
    const isFolder = item.type === 'FOLDER';
    const isUploading = item.isUploading;

    // Format size
    const formatSize = (bytes: number) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Number.parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    };

    return (
        <div
            onClick={() => isFolder && onFolderClick?.(item)}
            onKeyDown={(e) => {
                if (isFolder && (e.key === 'Enter' || e.key === ' ')) {
                    e.preventDefault();
                    onFolderClick?.(item);
                }
            }}
            role={isFolder ? "button" : undefined}
            tabIndex={isFolder ? 0 : undefined}
            className={`relative group flex flex-col justify-between p-4 rounded-xl border bg-card text-card-foreground shadow-sm hover:shadow-md transition-shadow ${isFolder ? 'cursor-pointer focus:ring-2 focus:ring-ring focus:outline-none' : ''}`}
        >
            {/* Header / Icon */}
            <div className="flex justify-between items-start mb-4">
                <div className={`p-3 rounded-lg ${isFolder ? 'bg-blue-500/10 text-blue-500' : 'bg-orange-500/10 text-orange-500'}`}>
                    {isFolder ? <Folder size={24} /> : <FileIcon size={24} />}
                </div>

                {!isUploading && (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8 -mr-2 text-muted-foreground">
                                <MoreVertical size={16} />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            {!isFolder && (
                                <DropdownMenuItem onClick={() => onDownload?.(item)}>
                                    <Download className="mr-2 h-4 w-4" /> Download
                                </DropdownMenuItem>
                            )}
                            <DropdownMenuItem onClick={() => onOptions?.(item)}>
                                <Info className="mr-2 h-4 w-4" /> Details
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                )}
                {isUploading && (
                    <div className="flex gap-1">
                        {item.uploadStatus === 'PAUSED' ? (
                            <Button variant="ghost" size="icon" className="h-6 w-6" onClick={(e) => { e.stopPropagation(); onResume?.(item); }}>
                                {/* Play Icon */}
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
                            </Button>
                        ) : (
                            <Button variant="ghost" size="icon" className="h-6 w-6" onClick={(e) => { e.stopPropagation(); onPause?.(item); }}>
                                {/* Pause Icon */}
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>
                            </Button>
                        )}
                        <Button variant="ghost" size="icon" className="h-6 w-6 text-destructive hover:text-destructive" onClick={(e) => { e.stopPropagation(); onCancel?.(item); }}>
                            {/* X Icon */}
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        </Button>
                    </div>
                )}
            </div>

            {/* Content */}
            <div className="space-y-1">
                <h4 className="font-medium truncate" title={item.name}>{item.name}</h4>
                <p className="text-xs text-muted-foreground">
                    {isFolder ? "Folder" : formatSize(item.size)}
                </p>

                {isUploading && (
                    <div className="mt-3 space-y-2">
                        <div className="flex justify-between text-xs text-muted-foreground">
                            <span>{item.uploadStatus === 'PENDING' ? 'Waiting...' : item.uploadStatus === 'PAUSED' ? 'Paused' : 'Uploading...'}</span>
                            <span>{item.uploadProgress}%</span>
                        </div>
                        {/* Use custom Progress component */}
                        <Progress value={item.uploadProgress} className="h-1.5" />
                    </div>
                )}
            </div>

            {/* Overlay Loader for Optimistic Creation if needed, but progress bar usually handles it */}
            {isUploading && item.uploadStatus === 'PENDING' && (
                <div className="absolute top-2 right-2">
                    <Loader2 size={16} className="animate-spin text-muted-foreground" />
                </div>
            )}
        </div>
    );
}
