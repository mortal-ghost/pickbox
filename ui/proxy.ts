import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function proxy(request: NextRequest) {
    const token = request.cookies.get("token")?.value;
    const { pathname } = request.nextUrl;

    // 1. Auth Guard: Protect /home and potentially other routes
    if (!token && pathname.startsWith("/home")) {
        return NextResponse.redirect(new URL("/auth?mode=login", request.url));
    }

    // 2. Guest Guard: Redirect logged-in users away from /auth and /
    if (token) {
        if (pathname === "/auth" || pathname === "/") {
            return NextResponse.redirect(new URL("/home", request.url));
        }
    }

    return NextResponse.next();
}

export const config = {
    matcher: ["/", "/auth", "/home/:path*"],
};
