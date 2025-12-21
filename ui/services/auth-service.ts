import Cookies from "js-cookie";
import { API_BASE_URL, ENDPOINTS } from "@/lib/constants";

export interface LoginRequest {
    email?: string;
    password?: string;
}

export interface RegisterRequest {
    username?: string;
    email?: string;
    password?: string;
}

export interface AuthResponse {
    token: string;
    id: number;
    username: string;
    email: string;
}

export const AuthService = {
    login: async (data: LoginRequest): Promise<AuthResponse> => {
        const response = await fetch(`${API_BASE_URL}${ENDPOINTS.AUTH.LOGIN}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || "Login failed");
        }

        return response.json();
    },

    register: async (data: RegisterRequest): Promise<AuthResponse> => {
        const response = await fetch(`${API_BASE_URL}${ENDPOINTS.AUTH.REGISTER}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || "Registration failed");
        }

        return response.json();
    },

    setToken: (token: string) => {
        Cookies.set("token", token, { expires: 7 });
    },

    getToken: (): string | null => {
        return Cookies.get("token") || null;
    },

    removeToken: () => {
        Cookies.remove("token");
    },

    getCurrentUser: async (): Promise<AuthResponse> => {
        const token = AuthService.getToken();
        if (!token) throw new Error("No token found");

        const response = await fetch(`${API_BASE_URL}${ENDPOINTS.AUTH.ME}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error("Failed to fetch user");
        }

        return response.json();
    },

    logout: () => {
        AuthService.removeToken();
    }
};
