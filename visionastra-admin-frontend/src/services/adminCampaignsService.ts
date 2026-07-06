import type {
  CampanaDetalle,
  CampanaPropietarioFiltro,
  CampanasPaginadas,
  CampanasQueryParams,
} from "@/types/adminCampaigns"

import adminApi from "./adminApi"

function buildCampanasParams(params: CampanasQueryParams) {
  const requestParams: Record<string, string | number> = {}
  const search = params.search?.trim()

  if (search) {
    requestParams.search = search
  }

  if (params.estado) {
    requestParams.estado = params.estado
  }

  if (typeof params.propietario === "number" && params.propietario > 0) {
    requestParams.propietario = params.propietario
  }

  if (params.page) {
    requestParams.page = params.page
  }

  return requestParams
}

export async function obtenerCampanas(
  params: CampanasQueryParams = {}
): Promise<CampanasPaginadas> {
  const response = await adminApi.get<CampanasPaginadas>("/campanas/", {
    params: buildCampanasParams(params),
  })

  return response.data
}

export async function obtenerCampana(id: number): Promise<CampanaDetalle> {
  const response = await adminApi.get<CampanaDetalle>(`/campanas/${id}/`)

  return response.data
}

export async function obtenerCampanasPropietarios(): Promise<
  CampanaPropietarioFiltro[]
> {
  const response = await adminApi.get<CampanaPropietarioFiltro[]>(
    "/campanas/propietarios/"
  )

  return response.data
}
