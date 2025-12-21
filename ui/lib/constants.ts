export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/pickbox/api";

export const ENDPOINTS = {
    AUTH: {
        LOGIN: "/auth/login",
        REGISTER: "/auth/register",
        ME: "/auth/me",
    },
    STORAGE: {
        UPLOAD: "/upload",
        INIT: "/upload/init",
        COMPLETE: "/upload/complete",
        DOWNLOAD: "/download",
    },
    HEALTH: "/health",
} as const;
