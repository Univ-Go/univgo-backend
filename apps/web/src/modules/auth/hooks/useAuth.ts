"use client";

import { useEffect, useState } from "react";

interface User {
  identification: string;
}

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const getUserFromToken = () => {
      try {
        const token = document.cookie
          .split("; ")
          .find((row) => row.startsWith("token="))
          ?.split("=")[1];

        if (token) {
          const payload = JSON.parse(atob(token.split(".")[1]));

          setUser({
            identification: payload.identification,
          });
        }
      } catch (error) {
        console.error("Error decoding token:", error);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    getUserFromToken();
  }, []);
  return {
    user,
    loading,
    isAuthenticated: !!user,
  };
};
