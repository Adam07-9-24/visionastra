package com.tecsup.visionastra.mobile.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.visionastra.mobile.core.session.SessionManager
import com.tecsup.visionastra.mobile.core.session.SessionState
import com.tecsup.visionastra.mobile.ui.auth.LoginScreen
import com.tecsup.visionastra.mobile.ui.auth.LoginViewModel
import com.tecsup.visionastra.mobile.ui.dashboard.DashboardScreen
import com.tecsup.visionastra.mobile.ui.splash.SplashDestination
import com.tecsup.visionastra.mobile.ui.splash.SplashScreen
import com.tecsup.visionastra.mobile.ui.splash.SplashViewModel
import kotlinx.coroutines.launch

@Composable
fun VisionAstraNavHost(
    sessionManager: SessionManager
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionManager) {
        sessionManager.sessionExpiredEvents.collect {
            navController.navigate(AppDestination.Login.route) {
                popUpTo(AppDestination.Dashboard.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route
    ) {
        composable(AppDestination.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.destination) {
                when (state.destination) {
                    SplashDestination.Login -> {
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(AppDestination.Splash.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                    SplashDestination.Dashboard -> {
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Splash.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                    null -> Unit
                }
            }

            SplashScreen()
        }

        composable(AppDestination.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.loginSuccess) {
                if (state.loginSuccess) {
                    viewModel.onLoginSuccessHandled()
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }

            LoginScreen(
                state = state,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onPasswordVisibilityChange = viewModel::onPasswordVisibilityChange,
                onLoginClick = viewModel::login
            )
        }

        composable(AppDestination.Dashboard.route) {
            var isLoggingOut by remember { mutableStateOf(false) }
            val authenticatedState = sessionState as? SessionState.Authenticated

            LaunchedEffect(sessionState) {
                if (sessionState is SessionState.Unauthenticated) {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(AppDestination.Dashboard.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }

            if (authenticatedState != null) {
                DashboardScreen(
                    user = authenticatedState.user,
                    isLoggingOut = isLoggingOut,
                    onLogoutClick = {
                        if (!isLoggingOut) {
                            isLoggingOut = true
                            coroutineScope.launch {
                                sessionManager.logout()
                                isLoggingOut = false
                                navController.navigate(AppDestination.Login.route) {
                                    popUpTo(AppDestination.Dashboard.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            } else {
                SplashScreen()
            }
        }
    }
}
