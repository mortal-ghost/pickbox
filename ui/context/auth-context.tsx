"use client";

import { createContext, useContext, useEffect, useState, useMemo } from "react";
import { AuthService, AuthResponse } from "@/services/auth-service";

interface AuthContextType {
    user: AuthResponse | null;
    loading: boolean;
    login: (token: string, user: AuthResponse) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: Readonly<{ children: React.ReactNode }>) {
    const [user, setUser] = useState<AuthResponse | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const initAuth = async () => {
            const token = AuthService.getToken();
            if (token) {
                try {
                    const userData = await AuthService.getCurrentUser();
                    setUser(userData);
                } catch (error) {
                    console.error("Failed to restore session:", error);
                    AuthService.removeToken();
                }
            }
            setLoading(false);
        };

        initAuth();
    }, []);

    const login = (token: string, userData: AuthResponse) => {
        AuthService.setToken(token);
        setUser(userData);
    };

    const logout = () => {
        AuthService.removeToken();
        setUser(null);
    };

    const value = useMemo(() => ({
        user,
        loading,
        login,
        logout
    }), [user, loading]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}
