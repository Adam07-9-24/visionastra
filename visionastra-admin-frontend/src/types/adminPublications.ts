export type PublicacionEstado =
  | "borrador"
  | "lista"
  | "programada"
  | "enviada"
  | "publicada"
  | "error"
  | "cancelada"

export interface PublicacionCampana {
  idCampana: number
  nombre: string
}

export interface PublicacionUsuario {
  idUsuario: number
  nombres: string
  apellidos: string
  email: string
}

export interface PublicacionUsuarioFiltro extends PublicacionUsuario {
  totalPublicaciones: number
}

export interface PublicacionAdmin {
  idPublicacion: number
  titulo: string
  campana: PublicacionCampana
  usuario: PublicacionUsuario
  estado: PublicacionEstado
  mensajeError: string | null
  fechaCreacion: string | null
}

export interface PublicacionesPaginadas {
  count: number
  next: string | null
  previous: string | null
  results: PublicacionAdmin[]
}

export interface PublicacionesQueryParams {
  search?: string
  estado?: PublicacionEstado | ""
  usuario?: number | ""
  page?: number
}
