"use client";


import { useState, useEffect, Suspense } from "react";

import { useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Navbar } from "@/components/landing/navbar";

function AuthContent() {
    const searchParams = useSearchParams();
    const [isRegister, setIsRegister] = useState(false);

    useEffect(() => {
        const mode = searchParams.get("mode");
        if (mode === "register") {
            setIsRegister(true);
        } else if (mode === "login") {
            setIsRegister(false);
        }
    }, [searchParams]);

    return (
        <div className="flex min-h-screen flex-col bg-background">
            <Navbar />

            <main className="flex-1 flex items-center justify-center py-12 md:py-24 lg:py-32">
                <div className="flex flex-col items-center max-w-sm w-full space-y-8 px-4">
                    <div className="text-center space-y-2">
                        <h1 className="text-3xl font-bold tracking-tight text-foreground">
                            {isRegister ? "Create an account" : "Welcome back"}
                        </h1>
                        <p className="text-muted-foreground">
                            {isRegister
                                ? "Enter your details to get started."
                                : "Enter your credentials to access your account."}
                        </p>
                    </div>

                    <div className="w-full space-y-4">
                        {isRegister && (
                            <div className="space-y-2">
                                <Input
                                    type="text"
                                    placeholder="Username"
                                    className="bg-secondary/50"
                                />
                            </div>
                        )}
                        <div className="space-y-2">
                            <Input
                                type="email"
                                placeholder="Email"
                                className="bg-secondary/50"
                            />
                        </div>
                        <div className="space-y-2">
                            <Input
                                type="password"
                                placeholder="Password"
                                className="bg-secondary/50"
                            />
                        </div>

                        <Button className="w-full" size="lg">
                            {isRegister ? "Sign Up" : "Sign In"}
                        </Button>
                    </div>

                    <div className="text-center text-sm text-muted-foreground">
                        {isRegister ? (
                            <>
                                Already have an account?{" "}
                                <Button
                                    variant="link"
                                    className="p-0 h-auto font-semibold text-primary"
                                    onClick={() => setIsRegister(false)}
                                >
                                    Login
                                </Button>
                            </>
                        ) : (
                            <>
                                Don&apos;t have an account?{" "}
                                <Button
                                    variant="link"
                                    className="p-0 h-auto font-semibold text-primary"
                                    onClick={() => setIsRegister(true)}
                                >
                                    Register
                                </Button>
                            </>
                        )}
                    </div>
                </div>
            </main>

            <footer className="border-t py-6 md:py-0">
                <div className="container flex flex-col items-center justify-between gap-4 md:h-24 md:flex-row mx-auto px-4">
                    <p className="text-center text-sm leading-loose text-muted-foreground md:text-left">
                        © {new Date().getFullYear()} Pickbox. All rights reserved.
                    </p>
                </div>
            </footer>
        </div>
    );
}

export default function AuthPage() {
    return (
        <Suspense>
            <AuthContent />
        </Suspense>
    );
}
