"use client";

import { FileItem } from "@/lib/types";
import { File as FileIcon, Folder, MoreVertical, Download, Info } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface FileRowProps {
    readonly item: FileItem;
    readonly onDownload?: (item: FileItem) => void;
    readonly onOptions?: (item: FileItem) => void;
    readonly onFolderClick?: (item: FileItem) => void;
    readonly onPause?: (item: FileItem) => void;
    readonly onResume?: (item: FileItem) => void;
    readonly onCancel?: (item: FileItem) => void;
}

export function FileRow({ item, onDownload, onOptions, onFolderClick, onPause, onResume, onCancel }: FileRowProps) {
    const isFolder = item.type === 'FOLDER';
    const isUploading = item.isUploading;

    const formatSize = (bytes: number) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Number.parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    };

    const formatDate = (date?: string | number) => {
        if (!date) return '-';
        return new Date(date).toLocaleDateString();
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
            className={`group flex items-center justify-between p-3 rounded-lg border-b last:border-0 hover:bg-muted/30 transition-colors ${isFolder ? 'cursor-pointer focus:ring-2 focus:ring-ring focus:outline-none' : ''}`}
        >
            <div className="flex items-center gap-4 flex-1 min-w-0">
                <div className={`p-2 rounded-lg ${isFolder ? 'bg-blue-500/10 text-blue-500' : 'bg-orange-500/10 text-orange-500'}`}>
                    {isFolder ? <Folder size={20} /> : <FileIcon size={20} />}
                </div>

                <div className="flex-1 min-w-0">
                    <h4 className="font-medium text-sm truncate" title={item.name}>{item.name}</h4>
                    {isUploading ? (
                        <div className="flex items-center gap-2 mt-1">
                            <div className="h-1 w-24 bg-secondary rounded-full overflow-hidden">
                                <div
                                    className="h-full bg-primary transition-all duration-300"
                                    style={{ width: `${item.uploadProgress || 0}%` }}
                                />
                            </div>
                            <span className="text-[10px] text-muted-foreground">{item.uploadProgress}% {item.uploadStatus === 'PAUSED' ? '(Paused)' : ''}</span>
                        </div>
                    ) : (
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <span>{isFolder ? "Folder" : formatSize(item.size)}</span>
                            <span>•</span>
                            <span>{formatDate(item.updatedAt)}</span>
                        </div>
                    )}
                </div>
            </div>

            <div className="flex items-center gap-2">
                {isUploading && (
                    <div className="flex gap-1 items-center">
                        {item.uploadStatus === 'PAUSED' ? (
                            <Button variant="ghost" size="icon" className="h-6 w-6" onClick={(e) => { e.stopPropagation(); onResume?.(item); }}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
                            </Button>
                        ) : (
                            <Button variant="ghost" size="icon" className="h-6 w-6" onClick={(e) => { e.stopPropagation(); onPause?.(item); }}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>
                            </Button>
                        )}
                        <Button variant="ghost" size="icon" className="h-6 w-6 text-destructive hover:text-destructive" onClick={(e) => { e.stopPropagation(); onCancel?.(item); }}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        </Button>
                    </div>
                )}

                {!isUploading && (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity">
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
            </div>
        </div>
    );
}
