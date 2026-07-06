import { AxiosError } from "axios"

type ApiErrorBody = {
  detail?: unknown
  mensaje?: unknown
}

const STATUS_MESSAGES: Record<number, string> = {
  401: "No tienes permisos para realizar esta acción.",
  403: "No tienes permisos para realizar esta acción.",
  404: "El usuario no existe.",
  502: "No fue posible completar la operación en el servicio principal.",
  503: "El servicio principal no está disponible temporalmente.",
  504: "El servicio principal tardó demasiado en responder.",
}

function getStringField(value: unknown) {
  return typeof value === "string" && value.trim() ? value : null
}

export function getAdminApiErrorMessage(
  error: unknown,
  fallback = "Ocurrió un error inesperado."
) {
  if (error instanceof AxiosError) {
    const data = error.response?.data as ApiErrorBody | undefined
    const detail = getStringField(data?.detail)
    const mensaje = getStringField(data?.mensaje)

    if (detail) {
      return detail
    }

    if (mensaje) {
      return mensaje
    }

    if (error.response?.status && STATUS_MESSAGES[error.response.status]) {
      return STATUS_MESSAGES[error.response.status]
    }
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message
  }

  return fallback
}
