package com.tecsup.visionastra.mobile.ui.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)
