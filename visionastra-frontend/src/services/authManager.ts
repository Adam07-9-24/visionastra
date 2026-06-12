import axios from "axios";

type RefreshResponse = {
  token: string;
  refreshToken: string;
  type?: string;
};

let refreshPromise: Promise<string> | null = null;

export const authManager = {
  isRefreshing: () => refreshPromise !== null,

  refreshToken: async (): Promise<string> => {
    if (refreshPromise) {
      return refreshPromise;
    }

    refreshPromise = (async () => {
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

      return newToken;
    })();

    try {
      return await refreshPromise;
    } finally {
      refreshPromise = null;
    }
  },

  clearSession: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");

    localStorage.removeItem("nombres");
    localStorage.removeItem("apellidos");
    localStorage.removeItem("email");
    localStorage.removeItem("rol");
    localStorage.removeItem("idSesion");
    localStorage.removeItem("nombreUsuario");
    localStorage.removeItem("nombre");

    window.location.href = "/login";
  },
};
