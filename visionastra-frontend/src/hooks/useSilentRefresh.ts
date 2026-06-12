import { useEffect, useRef } from "react";
import { authManager } from "@/services/authManager";

export function useSilentRefresh() {
  const timeoutRef = useRef<number | null>(null);

  useEffect(() => {
    const clearRefreshTimer = () => {
      if (timeoutRef.current !== null) {
        window.clearTimeout(timeoutRef.current);
        timeoutRef.current = null;
      }
    };

    const scheduleSilentRefresh = (token: string) => {
      clearRefreshTimer();

      try {
        const payload = JSON.parse(atob(token.split(".")[1]));

        if (!payload.exp) {
          throw new Error("JWT sin exp");
        }

        const exp = payload.exp * 1000;
        const now = Date.now();
        const timeLeft = exp - now;

        // Refrescar 5 minutos antes de expirar
        const refreshTime = timeLeft - 5 * 60 * 1000;

        const executeRefresh = async () => {
          try {
            const newToken = await authManager.refreshToken();

            scheduleSilentRefresh(newToken);
          } catch {
            authManager.clearSession();
          }
        };

        // Si el token ya está cerca de vencer, refrescar inmediatamente
        if (refreshTime <= 0) {
          timeoutRef.current = window.setTimeout(() => {
            void executeRefresh();
          }, 0);

          return;
        }

        timeoutRef.current = window.setTimeout(() => {
          void executeRefresh();
        }, refreshTime);
      } catch {
        authManager.clearSession();
      }
    };

    const token = localStorage.getItem("token");

    if (!token) {
      return;
    }

    scheduleSilentRefresh(token);

    return () => {
      clearRefreshTimer();
    };
  }, []);
}
