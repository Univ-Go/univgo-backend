"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Cookies from "js-cookie";
import toast from "react-hot-toast";
import Logo from "@/src/shared/components/common/Logo";
import InputForm from "@/src/shared/components/ui/InputForm";
import Checkbox from "@/src/shared/components/ui/Checkbox";
import { Button } from "@/src/shared/components/ui/Button";

export function Login() {
  const [identification, setIdentification] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const handleLogin = async () => {
    // Prevenir múltiples envíos
    if (isLoading) return;
    
    // Validación básica antes de iniciar loading
    if (identification.length === 0 || password.length === 0) {
      toast.error("Rellene todos los campos solicitados", {
        duration: 3000,
        position: "top-center",
      });
      return;
    }

    setIsLoading(true);

    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/sign-in`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identification, password }),
      });

      if (!res.ok) {
        toast.error("Usuario o contraseña incorrectos", {
          duration: 3000,
          position: "top-center",
        });
        return;
      }

      const data = await res.json();
      const token = data.access_token;

      // Save bearer token in a cookie with different expiration based on "Remember me"
      const cookieOptions = {
        expires: rememberMe ? 30 : 1, // 30 days if remember me, 1 day if not
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict" as const,
      };

      Cookies.set("token", token, cookieOptions);

      toast.success(
        rememberMe 
          ? "Iniciando sesión... (recordado por 30 días)" 
          : "Iniciando sesión...", 
        {
          duration: 3000,
          position: "top-center",
        }
      );

      // Don't set loading to false on success - keep button disabled until redirect
      router.push("/main");
      return; // Exit early on success
    } catch {
      toast.error("Error de conexión. Intente nuevamente", {
        duration: 3000,
        position: "top-center",
      });
      // Only set loading to false on error
      setIsLoading(false);
    }
  };

  return (
    <>
      <div className="flex min-h-screen items-center justify-center bg-[url(/imagen_u_medellin_2.jpg)] bg-cover bg-center bg-fixed bg-no-repeat relative">
        {/* Capa de oscurecimiento con viñeta */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-black/10"></div>

        <div className="w-full max-w-sm sm:max-w-md lg:max-w-lg mx-auto px-4 sm:px-6 relative z-10">
          {/* Tarjeta semitransparente */}
          <div className="flex items-center justify-center">
            <div className="backdrop-blur-md bg-white/75 p-6 sm:p-8 lg:p-10 rounded-xl shadow-[0_10px_50px_rgba(0,0,0,0.5)] w-full border border-white/20">
              {/* Logo centrado */}
              <div className="flex flex-col items-center mb-4 sm:mb-6">
                <Logo
                  imageSource="/udem_logo.png"
                  alt="Logo de La Universidad de Medellín"
                  width={100}
                  height={100}
                  styles="mb-4 sm:mb-6 w-20 h-20 sm:w-24 sm:h-24 lg:w-28 lg:h-28"
                />
                <h1 className="text-xl sm:text-2xl lg:text-3xl font-bold text-[var(--text-color)]">
                  UnivGo
                </h1>
                <p className="text-xs sm:text-sm text-[var(--secondary-text)] text-center mt-1">
                  Tu plataforma universitaria
                </p>
              </div>

              {/* Formulario */}
              <div className="w-full space-y-4 sm:space-y-6">
                <InputForm
                  label="Documento de identidad"
                  id="documentoIdentidad"
                  placeholder="Ingrese su documento"
                  typeInput="text"
                  value={identification}
                  onChange={(e) => setIdentification(e.target.value)}
                />

                <InputForm
                  label="Contraseña"
                  id="contrasena"
                  placeholder="Ingrese su contraseña"
                  typeInput="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />

                {/* Opciones adicionales */}
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 sm:gap-0">
                  {/* Checkbox personalizado */}
                  <Checkbox 
                    id="remember-me" 
                    label="Recordarme" 
                    checked={rememberMe}
                    onChange={setRememberMe}
                  />

                  <a
                    href=""
                    className="text-xs sm:text-sm font-medium text-[var(--primary-blue)] hover:underline focus:outline-none focus:ring-2 focus:ring-[var(--primary-blue)] focus:ring-offset-2 rounded-sm"
                  >
                    Olvidé mi Contraseña
                  </a>
                </div>

                {/* Botón de acceso */}
                <div className="pt-2 sm:pt-4 flex justify-center">
                  <Button 
                    text="Entrar" 
                    onClick={handleLogin} 
                    loading={isLoading}
                    disabled={isLoading}
                  />
                </div>

                {/* Footer con información de la app */}
                <div className="border-t border-[var(--primary-gray)] pt-3 sm:pt-4 mt-4 sm:mt-6">
                  <p className="text-xs text-center text-[var(--secondary-text)]">
                    Reserva espacios deportivos, consulta tu información
                    académica y mucho más.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
