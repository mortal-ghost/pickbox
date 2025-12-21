"use client";

import { useState, useRef } from "react";
import { Plus, File, Folder, X } from "lucide-react";
import { Button } from "@/components/ui/button";

interface UploadFabProps {
    onFileUpload: (files: FileList) => void;
    onCreateFolder: () => void;
}

export function UploadFab({ onFileUpload, onCreateFolder }: Readonly<UploadFabProps>) {
    const [isOpen, setIsOpen] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const toggleOpen = () => setIsOpen(!isOpen);

    const handleFileClick = () => {
        fileInputRef.current?.click();
        setIsOpen(false);
    };

    const handleFolderClick = () => {
        onCreateFolder();
        setIsOpen(false);
    };

    const onFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            onFileUpload(e.target.files);
        }
        // Reset value to allow selecting same file again
        e.target.value = "";
    };

    return (
        <div className="fixed bottom-8 right-8 flex flex-col items-end gap-4 z-50">
            {/* Hidden Input for Files */}
            <input
                type="file"
                ref={fileInputRef}
                className="hidden"
                multiple
                onChange={onFileChange}
            />

            {/* Menu Options */}
            {isOpen && (
                <div className="flex flex-col gap-3 mb-2 animate-in slide-in-from-bottom-5 fade-in duration-200">
                    <Button
                        variant="secondary"
                        className="flex items-center gap-2 pr-6 pl-4 shadow-lg border border-border"
                        onClick={handleFolderClick}
                    >
                        <div className="p-1 bg-blue-500/10 rounded text-blue-500">
                            <Folder size={20} />
                        </div>
                        <span>Folder</span>
                    </Button>

                    <Button
                        variant="secondary"
                        className="flex items-center gap-2 pr-6 pl-4 shadow-lg border border-border"
                        onClick={handleFileClick}
                    >
                        <div className="p-1 bg-green-500/10 rounded text-green-500">
                            <File size={20} />
                        </div>
                        <span>File</span>
                    </Button>
                </div>
            )}

            {/* FAB */}
            <Button
                size="icon"
                className="h-14 w-14 rounded-full shadow-xl bg-primary hover:bg-primary/90 transition-transform active:scale-95"
                onClick={toggleOpen}
            >
                {isOpen ? <X size={24} /> : <Plus size={24} />}
            </Button>
        </div>
    );
}
