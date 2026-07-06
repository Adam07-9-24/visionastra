import type {
  AdminLoginRequest,
  AdminLoginResponse,
  AdminUser,
  ApiMessageResponse,
} from "@/types/adminAuth"

import adminApi from "./adminApi"

function getCookie(name: string): string | null {
  const cookies = document.cookie ? document.cookie.split(";") : []

  for (const cookie of cookies) {
    const trimmedCookie = cookie.trim()
    const separatorIndex = trimmedCookie.indexOf("=")

    if (separatorIndex === -1) {
      continue
    }

    const cookieName = trimmedCookie.slice(0, separatorIndex)

    if (cookieName === name) {
      return decodeURIComponent(trimmedCookie.slice(separatorIndex + 1))
    }
  }

  return null
}

export async function ensureCsrfCookie(): Promise<string> {
  await adminApi.get<ApiMessageResponse>("/auth/csrf/")

  const csrfToken = getCookie("csrftoken")

  if (!csrfToken) {
    throw new Error("No se pudo obtener la protección CSRF.")
  }

  return csrfToken
}

export async function loginAdmin(
  data: AdminLoginRequest
): Promise<AdminLoginResponse> {
  const csrfToken = await ensureCsrfCookie()
  const response = await adminApi.post<AdminLoginResponse>("/auth/login/", data, {
    headers: {
      "X-CSRFToken": csrfToken,
    },
  })

  return response.data
}

export async function getCurrentAdmin(): Promise<AdminUser> {
  const response = await adminApi.get<AdminUser>("/auth/me/")

  return response.data
}

export async function logoutAdmin(): Promise<ApiMessageResponse> {
  const csrfToken = await ensureCsrfCookie()
  const response = await adminApi.post<ApiMessageResponse>(
    "/auth/logout/",
    {},
    {
      headers: {
        "X-CSRFToken": csrfToken,
      },
    }
  )

  return response.data
}
