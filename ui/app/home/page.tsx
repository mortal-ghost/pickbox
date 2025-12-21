"use client";

import { useAuth } from "@/context/auth-context";
import { Navbar } from "@/components/landing/navbar";

import { SearchBar } from "./search-bar";
import { UploadFab } from "@/components/home/upload-fab";
import { LayoutGrid, List } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";

export default function HomePage() {
    const { user } = useAuth();
    const [searchQuery, setSearchQuery] = useState("");
    const [currentFolderId, setCurrentFolderId] = useState<string | null>(null); // For future folder navigation
    const [viewMode, setViewMode] = useState<"grid" | "list">("grid");

    const handleSearch = (query: string) => {
        setSearchQuery(query);
        console.log("Searching for:", query);
        // Implement actual search logic later
    };

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
                        <p className="text-sm text-muted-foreground mb-4">
                            {currentFolderId ? `Current Folder: ${currentFolderId}` : "Root Directory"}
                        </p>
                        <p className="text-sm text-muted-foreground">
                            {searchQuery ? `Searching for: "${searchQuery}"` : "You have 0 files stored."}
                        </p>
                        {/* Placeholder for scroll testing */}
                        {Array.from({ length: 20 }).map((_, i) => (
                            <div key={i} className="py-2 border-b last:border-0 border-border/40 text-sm text-muted-foreground">
                                Placeholder File Item {i + 1}
                            </div>
                        ))}
                    </div>
                </div>
            </main>
            <UploadFab
                onFileUpload={(files) => {
                    console.log("File upload:", files);
                }}
                onCreateFolder={() => {
                    console.log("Create folder triggered");
                }}
            />
        </div>
    );
}
