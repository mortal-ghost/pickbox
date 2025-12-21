"use client";

import { useAuth } from "@/context/auth-context";
import { Navbar } from "@/components/landing/navbar";

export default function HomePage() {
    const { user } = useAuth();

    return (
        <div className="flex min-h-screen flex-col bg-background">
            {/* Custom Navbar for Home - reusing landing navbar for now but should probably differ */}
            {/* For now, let's keep it simple and just show content */}
            <Navbar />

            <main className="container mx-auto px-4 py-8">
                <div className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold">Dashboard</h1>
                    <div className="flex items-center gap-4">
                        <span>Welcome, {user?.username}</span>
                    </div>
                </div>

                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {/* Placeholder for future content */}
                    <div className="p-6 rounded-lg border bg-card text-card-foreground shadow-sm">
                        <h3 className="font-semibold leading-none tracking-tight mb-2">My Files</h3>
                        <p className="text-sm text-muted-foreground">You have 0 files stored.</p>
                    </div>
                </div>
            </main>
        </div>
    );
}
