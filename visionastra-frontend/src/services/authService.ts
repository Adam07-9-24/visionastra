import api from "./api";
import publicApi from "./publicApi";

export type RegisterRequest = {
  nombres: string;
  apellidos: string;
  email: string;
  password: string;
};

export type RegisterResponse = {
  mensaje: string;
};

export const login = async (email: string, password: string) => {
  const res = await api.post("/auth/login", {
    email,
    password,
  });

  return res.data;
};

export const logout = async () => {
  const res = await api.post("/auth/logout");
  return res.data;
};

export async function register(
  data: RegisterRequest
): Promise<RegisterResponse> {
  const response = await publicApi.post<RegisterResponse>(
    "/auth/register",
    data
  );

  return response.data;
}
