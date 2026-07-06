package com.tecsup.visionastra.mobile.data.remote.dto

data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String,
    val type: String?
)
