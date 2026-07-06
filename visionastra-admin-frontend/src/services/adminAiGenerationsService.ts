import type {
  GeneracionIAUsuarioFiltro,
  GeneracionesIAPaginadas,
  GeneracionesIAQueryParams,
} from "@/types/adminAiGenerations"

import adminApi from "./adminApi"

function buildGeneracionesIAParams(params: GeneracionesIAQueryParams) {
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

export async function obtenerGeneracionesIA(
  params: GeneracionesIAQueryParams = {}
): Promise<GeneracionesIAPaginadas> {
  const response = await adminApi.get<GeneracionesIAPaginadas>(
    "/generaciones-ia/",
    {
      params: buildGeneracionesIAParams(params),
    }
  )

  return response.data
}

export async function obtenerGeneracionesIAUsuarios(): Promise<
  GeneracionIAUsuarioFiltro[]
> {
  const response = await adminApi.get<GeneracionIAUsuarioFiltro[]>(
    "/generaciones-ia/usuarios/"
  )

  return response.data
}
