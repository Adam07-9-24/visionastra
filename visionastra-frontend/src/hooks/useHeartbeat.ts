import { useEffect, useRef } from "react";
import api from "@/services/api";
import { authManager } from "@/services/authManager";

export function useHeartbeat() {
  const lastActivity = useRef<number>(0);

  useEffect(() => {
    lastActivity.current = Date.now();

    const updateActivity = () => {
      lastActivity.current = Date.now();
    };

    // 🔥 Eventos de actividad
    window.addEventListener("mousemove", updateActivity);
    window.addEventListener("keydown", updateActivity);
    window.addEventListener("scroll", updateActivity);
    window.addEventListener("click", updateActivity);

    // 🔥 Detectar cambio de pestaña
    const handleVisibility = () => {
      if (document.visibilityState === "visible") {
        lastActivity.current = Date.now();
        console.log("👁️ Usuario volvió a la app");
      }
    };

    document.addEventListener("visibilitychange", handleVisibility);

    // 🔥 Heartbeat inteligente
    const interval = setInterval(() => {
      const token = localStorage.getItem("token");

      if (!token) return; // 🔥 NO ejecuta si no hay sesión
      if (authManager.isRefreshing()) return;

      const now = Date.now();
      const INACTIVITY_LIMIT = 5 * 60 * 1000; // 5 min

      if (document.visibilityState === "hidden") {
        console.log("🛑 pestaña oculta, no enviar heartbeat");
        return;
      }

      if (now - lastActivity.current < INACTIVITY_LIMIT) {
        api
          .post("/sesiones/heartbeat")
          .then(() => console.log("💓 heartbeat enviado"))
          .catch(() => console.log("❌ heartbeat error"));
      } else {
        console.log("⏸️ usuario inactivo");
      }
    }, 300000); // 5 min

    return () => {
      clearInterval(interval);

      window.removeEventListener("mousemove", updateActivity);
      window.removeEventListener("keydown", updateActivity);
      window.removeEventListener("scroll", updateActivity);
      window.removeEventListener("click", updateActivity);

      document.removeEventListener("visibilitychange", handleVisibility);
    };
  }, []);
}
