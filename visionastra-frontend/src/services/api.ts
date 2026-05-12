import axios from "axios";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { authManager } from "./authManager";

const api = axios.create({
  baseURL: "http://localhost:8083/api",
});

type QueueItem = {
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
};

type RefreshResponse = {
  token: string;
  refreshToken: string;
  type?: string;
};

let failedQueue: QueueItem[] = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// 🔐 REQUEST → agregar JWT
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// 🔁 RESPONSE → refresh automático seguro
api.interceptors.response.use(
  (response) => response,

  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (!originalRequest) {
      return Promise.reject(error);
    }

    // 🔥 evitar loop infinito
    if (originalRequest.url?.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    // 🔥 manejar 401
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (authManager.isRefreshing()) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        });
      }

      originalRequest._retry = true;
      authManager.startRefresh();

      try {
        const refreshToken = localStorage.getItem("refreshToken");

        if (!refreshToken) {
          throw new Error("No refresh token disponible");
        }

        const res = await axios.post<RefreshResponse>(
          "http://localhost:8083/api/auth/refresh",
          { refreshToken }
        );

        const newToken = res.data.token;
        const newRefreshToken = res.data.refreshToken;

        if (!newToken || !newRefreshToken) {
          throw new Error("Respuesta de refresh incompleta");
        }

        // 🔥 IMPORTANTE: guardar AMBOS
        localStorage.setItem("token", newToken);
        localStorage.setItem("refreshToken", newRefreshToken);

        processQueue(null, newToken);

        originalRequest.headers.Authorization = `Bearer ${newToken}`;

        return api(originalRequest);
      } catch (err) {
        processQueue(err, null);

        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("user");

        window.location.href = "/login";

        return Promise.reject(err);
      } finally {
        authManager.endRefresh();
      }
    }

    return Promise.reject(error);
  }
);

export default api;
