export type AdminUser = {
  id: number
  username: string
  email: string
  isStaff: boolean
  isSuperuser: boolean
}

export type AdminLoginRequest = {
  username: string
  password: string
}

export type AdminLoginResponse = {
  mensaje: string
  usuario: AdminUser
}

export type ApiMessageResponse = {
  mensaje: string
}
