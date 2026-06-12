import axios from "axios";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { authManager } from "./authManager";

const api = axios.create({
  baseURL: "http://localhost:8083/api",
});

// 🔐 REQUEST → agregar JWT
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// 🔁 RESPONSE → si hay 401, intentar refresh una sola vez
api.interceptors.response.use(
  (response) => response,

  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (!originalRequest) {
      return Promise.reject(error);
    }

    // Evitar loop infinito si falla el propio refresh
    if (originalRequest.url?.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    // Si el access token venció, intentar refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const newToken = await authManager.refreshToken();

        originalRequest.headers.Authorization = `Bearer ${newToken}`;

        return api(originalRequest);
      } catch (err) {
        authManager.clearSession();
        return Promise.reject(err);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
