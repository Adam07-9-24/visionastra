package com.tecsup.visionastra.mobile.data.remote.dto

data class LoginResponse(
    val idUsuario: Long,
    val nombres: String,
    val apellidos: String?,
    val email: String,
    val rol: String,
    val estado: String,
    val mensaje: String?,
    val token: String,
    val refreshToken: String
)
