import { NextResponse } from "next/server";
import { NextRequest } from "next/server";
import { jwtVerify } from "jose";

const secret = new TextEncoder().encode(process.env.JWT_SECRET);

export async function middleware(request: NextRequest) {
  const jwt = request.cookies.get("token")?.value;

  if (!jwt) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  try {
    await jwtVerify(jwt, secret);
    return NextResponse.next();
  } catch (error: unknown) {
    console.error("❌ JWT verification error: ", error);
    // Type guard para error con code
    if (typeof error === 'object' && error !== null && 'code' in error) {
      const err = error as { code?: string };
      if (err.code === 'ERR_JWT_EXPIRED') {
        const response = NextResponse.redirect(new URL("/", request.url));
        response.cookies.delete("token");
        response.headers.set("x-session-expired", "true");
        return response;
      }
    }
    return NextResponse.redirect(new URL("/", request.url));
  }
}

export const config = {
  matcher: ["/main/:path*", "/my-reservations/:path*", "/sports/:path*"],
};
