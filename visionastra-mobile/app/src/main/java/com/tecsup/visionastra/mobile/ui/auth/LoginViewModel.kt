package com.tecsup.visionastra.mobile.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.data.repository.AuthRepository
import com.tecsup.visionastra.mobile.data.repository.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, errorMessage = null, loginSuccess = false)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, errorMessage = null, loginSuccess = false)
        }
    }

    fun onPasswordVisibilityChange() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun login() {
        val currentState = _uiState.value
        val validationError = validate(currentState.email, currentState.password)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, loginSuccess = false)
            }

            when (
                val result = authRepository.login(
                    email = currentState.email.trim(),
                    password = currentState.password
                )
            ) {
                LoginResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, password = "", loginSuccess = true)
                    }
                }

                is LoginResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onLoginSuccessHandled() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    private fun validate(email: String, password: String): String? =
        when {
            email.isBlank() -> "El correo es obligatorio."
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Ingresa un correo valido."
            password.isBlank() -> "La contraseña es obligatoria."
            else -> null
        }
}
