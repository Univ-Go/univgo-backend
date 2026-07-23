"use client";

import { Login } from "@/src/modules/auth/components/Login";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Cookies from "js-cookie";

export default function Home() {
  const router = useRouter();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // Verificar si el usuario ya está autenticado
    const token = Cookies.get("token");
    
    if (token) {
      // Si hay token, redirigir a la página principal protegida
      router.push("/main");
    } else {
      // Si no hay token, mostrar el login
      setIsChecking(false);
    }
  }, [router]);

  // Mostrar loading mientras se verifica la autenticación
  if (isChecking) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[url(/imagen_u_medellin_2.jpg)] bg-cover bg-center bg-fixed bg-no-repeat relative">
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-black/10"></div>
        <div className="relative z-10 flex flex-col items-center">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white"></div>
          <p className="mt-4 text-white text-sm">Verificando sesión...</p>
        </div>
      </div>
    );
  }

  return (
    <>
      <Login />
    </>
  );
}
