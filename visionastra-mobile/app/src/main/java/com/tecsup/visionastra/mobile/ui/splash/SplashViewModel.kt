package com.tecsup.visionastra.mobile.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SplashDestination {
    Login,
    Dashboard
}

data class SplashUiState(
    val destination: SplashDestination? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val restored = sessionManager.restoreSession()
            _uiState.value = SplashUiState(
                destination = if (restored) {
                    SplashDestination.Dashboard
                } else {
                    SplashDestination.Login
                }
            )
        }
    }
}
