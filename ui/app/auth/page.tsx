"use client";

import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Navbar } from "@/components/landing/navbar";
import { Spinner } from "@/components/ui/spinner";
import { AuthService } from "@/services/auth-service";
import { useAuth } from "@/context/auth-context";

function AuthContent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const { login } = useAuth();

    const [isRegister, setIsRegister] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");

    const [formData, setFormData] = useState({
        username: "",
        email: "",
        password: ""
    });

    useEffect(() => {
        const mode = searchParams.get("mode");
        if (mode === "register") {
            setIsRegister(true);
        } else if (mode === "login") {
            setIsRegister(false);
        }
        setError("");
        setFormData({ username: "", email: "", password: "" });
    }, [searchParams]);

    const handleSubmit = async () => {
        setError("");
        setIsLoading(true);

        try {
            if (isRegister) {
                const response = await AuthService.register({
                    username: formData.username,
                    email: formData.email,
                    password: formData.password
                });
                login(response.token, response);
                router.push("/");
            } else {
                const response = await AuthService.login({
                    email: formData.email,
                    password: formData.password
                });
                login(response.token, response);
                router.push("/");
            }
        } catch (err: any) {
            setError(err.message || "An error occurred");
        } finally {
            setIsLoading(false);
        }
    };

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
                        {error && (
                            <div className="p-3 text-sm text-destructive bg-destructive/10 rounded-md">
                                {error}
                            </div>
                        )}

                        {isRegister && (
                            <div className="space-y-2">
                                <Input
                                    type="text"
                                    placeholder="Username"
                                    className="bg-secondary/50"
                                    value={formData.username}
                                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                />
                            </div>
                        )}
                        <div className="space-y-2">
                            <Input
                                type="email"
                                placeholder="Email"
                                className="bg-secondary/50"
                                value={formData.email}
                                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            />
                        </div>
                        <div className="space-y-2">
                            <Input
                                type="password"
                                placeholder="Password"
                                className="bg-secondary/50"
                                value={formData.password}
                                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            />
                        </div>

                        <Button className="w-full" size="lg" onClick={handleSubmit} disabled={isLoading}>
                            {isLoading && <Spinner className="mr-2 h-4 w-4" />}
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
                                    onClick={() => {
                                        setIsRegister(false);
                                        router.replace("/auth?mode=login");
                                    }}
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
                                    onClick={() => {
                                        setIsRegister(true);
                                        router.replace("/auth?mode=register");
                                    }}
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
