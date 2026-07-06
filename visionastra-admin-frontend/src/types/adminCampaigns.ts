export type CampanaEstado = "borrador" | "activa" | "pausada" | "finalizada"

export interface CampanaPropietario {
  idUsuario: number
  nombres: string
  apellidos: string
  email: string
}

export interface CampanaPropietarioFiltro extends CampanaPropietario {
  totalCampanas: number
}

export interface CampanaAdmin {
  idCampana: number
  nombre: string
  estado: CampanaEstado
  fechaCreacion: string | null
  totalRecursos: number
  totalPublicaciones: number
  propietario: CampanaPropietario
}

export interface CampanaDetalle extends CampanaAdmin {
  objetivo: string | null
  descripcion: string | null
  presupuesto: string | null
  fechaInicio: string | null
  fechaFin: string | null
  fechaActualizacion: string | null
}

export interface CampanasPaginadas {
  count: number
  next: string | null
  previous: string | null
  results: CampanaAdmin[]
}

export interface CampanasQueryParams {
  search?: string
  estado?: CampanaEstado | ""
  propietario?: number | ""
  page?: number
}
