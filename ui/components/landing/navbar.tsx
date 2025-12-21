"use client";

import Link from "next/link";
import Image from "next/image";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/auth-context";
import { useRouter } from "next/navigation";

export function Navbar() {
    const { user, logout } = useAuth();
    const router = useRouter();

    const handleLogout = () => {
        logout();
        router.push("/");
    };

    return (
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60">
            <div className="container mx-auto flex h-16 items-center justify-between px-4">
                <Link href={user ? "/home" : "/"} className="flex items-center gap-2">
                    <div className="relative h-8 w-8">
                        <Image
                            src="/logo.svg"
                            alt="Pickbox Logo"
                            fill
                            className="object-contain"
                        />
                    </div>
                    <span className="text-xl font-bold tracking-tight">Pickbox</span>
                </Link>
                <div className="flex items-center gap-4">
                    {user ? (
                        <>
                            <span className="text-sm text-muted-foreground mr-2 hidden md:inline-block">
                                {user.username}
                            </span>
                            <Button variant="ghost" asChild>
                                <Link href="/home">Dashboard</Link>
                            </Button>
                            <Button variant="outline" onClick={handleLogout}>
                                Logout
                            </Button>
                        </>
                    ) : (
                        <>
                            <Button variant="ghost" asChild>
                                <Link href="/auth?mode=login">Login</Link>
                            </Button>
                            <Button asChild>
                                <Link href="/auth?mode=register">Register</Link>
                            </Button>
                        </>
                    )}
                </div>
            </div>
        </header>
    );
}
