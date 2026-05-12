import { useEffect, useRef } from "react";
import axios from "axios";
import { authManager } from "@/services/authManager";

type RefreshResponse = {
  token: string;
  refreshToken: string;
  type?: string;
};

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

        const exp = payload.exp * 1000;
        const now = Date.now();
        const timeLeft = exp - now;

        // Refrescar 1 minuto antes de expirar
        const refreshTime = timeLeft - 60000;

        if (refreshTime <= 0) {
          console.log(
            "⛔ Token muy cerca de expirar, no se programó silent refresh"
          );
          return;
        }

        timeoutRef.current = window.setTimeout(async () => {
          if (authManager.isRefreshing()) {
            setTimeout(() => {
              const latestToken = localStorage.getItem("token");

              if (latestToken) {
                scheduleSilentRefresh(latestToken);
              }
            }, 3000);

            return;
          }

          try {
            authManager.startRefresh();

            const currentRefreshToken = localStorage.getItem("refreshToken");

            if (!currentRefreshToken) {
              throw new Error("No refresh token disponible");
            }

            const res = await axios.post<RefreshResponse>(
              "http://localhost:8083/api/auth/refresh",
              { refreshToken: currentRefreshToken }
            );

            const newToken = res.data.token;
            const newRefreshToken = res.data.refreshToken;

            if (!newToken || !newRefreshToken) {
              throw new Error("Respuesta de refresh incompleta");
            }

            localStorage.setItem("token", newToken);
            localStorage.setItem("refreshToken", newRefreshToken);

            // Programar el siguiente refresh usando el nuevo JWT
            scheduleSilentRefresh(newToken);
          } catch {
            localStorage.removeItem("token");
            localStorage.removeItem("refreshToken");
            localStorage.removeItem("user");

            window.location.href = "/login";
          } finally {
            authManager.endRefresh();
          }
        }, refreshTime);
      } catch {
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("user");

        window.location.href = "/login";
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
