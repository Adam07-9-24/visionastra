export type GeneracionIAEstado =
  | "pendiente"
  | "procesando"
  | "completado"
  | "error"

export interface GeneracionIACampana {
  idCampana: number
  nombre: string
}

export interface GeneracionIAUsuario {
  idUsuario: number
  nombres: string
  apellidos: string
  email: string
}

export interface GeneracionIAUsuarioFiltro extends GeneracionIAUsuario {
  totalGeneraciones: number
}

export interface GeneracionIAAdmin {
  idGeneracion: number
  campana: GeneracionIACampana
  usuario: GeneracionIAUsuario
  estado: GeneracionIAEstado
  fechaCreacion: string | null
}

export interface GeneracionesIAPaginadas {
  count: number
  next: string | null
  previous: string | null
  results: GeneracionIAAdmin[]
}

export interface GeneracionesIAQueryParams {
  search?: string
  estado?: GeneracionIAEstado | ""
  usuario?: number | ""
  page?: number
}
