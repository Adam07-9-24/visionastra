const NETWORK_ERROR_PATTERNS = [
  "connection has been closed before response",
  "while sending request body",
  "connection reset",
  "econnreset",
  "socket hang up",
  "broken pipe",
  "network error",
  "timeout",
  "timed out",
]

const AUTH_ERROR_PATTERNS = [
  "access_denied",
  "unauthorized",
  "forbidden",
  "invalid_grant",
  "oauth",
  "401",
  "403",
]

const QUOTA_ERROR_PATTERNS = [
  "quota",
  "quota exceeded",
  "daily limit",
  "rate limit",
  "too many requests",
  "429",
]

const FILE_ERROR_PATTERNS = [
  "file not found",
  "archivo no encontrado",
  "no such file",
  "video not found",
]

function includesAnyPattern(value: string, patterns: string[]) {
  return patterns.some((pattern) => value.includes(pattern))
}

export function getPublicationErrorMessage(message: string | null) {
  const normalizedMessage = message?.trim().toLowerCase()

  if (!normalizedMessage) {
    return null
  }

  if (includesAnyPattern(normalizedMessage, NETWORK_ERROR_PATTERNS)) {
    return "No se pudo completar la publicación. Inténtalo nuevamente."
  }

  if (includesAnyPattern(normalizedMessage, AUTH_ERROR_PATTERNS)) {
    return "No se pudo autorizar la publicación en YouTube."
  }

  if (includesAnyPattern(normalizedMessage, QUOTA_ERROR_PATTERNS)) {
    return "No se pudo publicar porque se alcanzó temporalmente el límite de YouTube."
  }

  if (includesAnyPattern(normalizedMessage, FILE_ERROR_PATTERNS)) {
    return "No se encontró el video asociado a la publicación."
  }

  return "No se pudo completar la publicación."
}
