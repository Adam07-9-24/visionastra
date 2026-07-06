import type {
  PublicacionUsuarioFiltro,
  PublicacionesPaginadas,
  PublicacionesQueryParams,
} from "@/types/adminPublications"

import adminApi from "./adminApi"

function buildPublicacionesParams(params: PublicacionesQueryParams) {
  const requestParams: Record<string, string | number> = {}
  const search = params.search?.trim()

  if (search) {
    requestParams.search = search
  }

  if (params.estado) {
    requestParams.estado = params.estado
  }

  if (typeof params.usuario === "number" && params.usuario > 0) {
    requestParams.usuario = params.usuario
  }

  if (params.page) {
    requestParams.page = params.page
  }

  return requestParams
}

export async function obtenerPublicaciones(
  params: PublicacionesQueryParams = {}
): Promise<PublicacionesPaginadas> {
  const response = await adminApi.get<PublicacionesPaginadas>(
    "/publicaciones/",
    {
      params: buildPublicacionesParams(params),
    }
  )

  return response.data
}

export async function obtenerPublicacionesUsuarios(): Promise<
  PublicacionUsuarioFiltro[]
> {
  const response = await adminApi.get<PublicacionUsuarioFiltro[]>(
    "/publicaciones/usuarios/"
  )

  return response.data
}
