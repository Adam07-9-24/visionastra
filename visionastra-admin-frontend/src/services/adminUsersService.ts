import { ensureCsrfCookie } from "@/services/adminAuthService"
import type {
  UsuarioAdmin,
  UsuarioEstadoResponse,
  UsuariosPaginados,
  UsuariosQueryParams,
} from "@/types/adminUsers"

import adminApi from "./adminApi"

function buildUsuariosParams(params: UsuariosQueryParams) {
  const requestParams: Record<string, string | number> = {}
  const search = params.search?.trim()

  if (search) {
    requestParams.search = search
  }

  if (params.estado) {
    requestParams.estado = params.estado
  }

  if (params.page) {
    requestParams.page = params.page
  }

  return requestParams
}

async function getCsrfHeaders() {
  const csrfToken = await ensureCsrfCookie()

  return {
    "X-CSRFToken": csrfToken,
  }
}

export async function obtenerUsuarios(
  params: UsuariosQueryParams = {}
): Promise<UsuariosPaginados> {
  const response = await adminApi.get<UsuariosPaginados>("/usuarios/", {
    params: buildUsuariosParams(params),
  })

  return response.data
}

export async function obtenerUsuario(id: number): Promise<UsuarioAdmin> {
  const response = await adminApi.get<UsuarioAdmin>(`/usuarios/${id}/`)

  return response.data
}

export async function bloquearUsuario(
  id: number
): Promise<UsuarioEstadoResponse> {
  const headers = await getCsrfHeaders()
  const response = await adminApi.patch<UsuarioEstadoResponse>(
    `/usuarios/${id}/bloquear/`,
    {},
    { headers }
  )

  return response.data
}

export async function activarUsuario(
  id: number
): Promise<UsuarioEstadoResponse> {
  const headers = await getCsrfHeaders()
  const response = await adminApi.patch<UsuarioEstadoResponse>(
    `/usuarios/${id}/activar/`,
    {},
    { headers }
  )

  return response.data
}
