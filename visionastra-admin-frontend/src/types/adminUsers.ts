export type UsuarioEstado = "activo" | "bloqueado" | "pendiente"

export interface RolAdmin {
  idRole: number
  nombre: string
}

export interface UsuarioAdmin {
  idUsuario: number
  nombres: string
  apellidos: string
  email: string
  estado: UsuarioEstado
  ultimoLogin: string | null
  fechaCreacion: string | null
  fechaActualizacion: string | null
  rol: RolAdmin
}

export interface UsuariosPaginados {
  count: number
  next: string | null
  previous: string | null
  results: UsuarioAdmin[]
}

export interface UsuariosQueryParams {
  search?: string
  estado?: UsuarioEstado | ""
  page?: number
}

export interface UsuarioEstadoResponse {
  mensaje: string
  usuario: UsuarioAdmin
}
