package com.tecsup.visionastra.mobile.navigation

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.visionastra.mobile.core.session.SessionManager
import com.tecsup.visionastra.mobile.core.session.SessionState
import com.tecsup.visionastra.mobile.ui.auth.LoginScreen
import com.tecsup.visionastra.mobile.ui.auth.LoginViewModel
import com.tecsup.visionastra.mobile.ui.ai.AiGenerationDetailScreen
import com.tecsup.visionastra.mobile.ui.ai.AiGenerationDetailViewModel
import com.tecsup.visionastra.mobile.ui.ai.AiGenerationFormScreen
import com.tecsup.visionastra.mobile.ui.ai.AiGenerationFormViewModel
import com.tecsup.visionastra.mobile.ui.ai.AiGenerationListScreen
import com.tecsup.visionastra.mobile.ui.ai.AiGenerationListViewModel
import com.tecsup.visionastra.mobile.ui.campaigns.CampaignDetailScreen
import com.tecsup.visionastra.mobile.ui.campaigns.CampaignDetailViewModel
import com.tecsup.visionastra.mobile.ui.campaigns.CampaignFormScreen
import com.tecsup.visionastra.mobile.ui.campaigns.CampaignFormViewModel
import com.tecsup.visionastra.mobile.ui.campaigns.CampaignListScreen
import com.tecsup.visionastra.mobile.ui.campaigns.CampaignListViewModel
import com.tecsup.visionastra.mobile.ui.dashboard.DashboardScreen
import com.tecsup.visionastra.mobile.ui.resources.CopyFormScreen
import com.tecsup.visionastra.mobile.ui.resources.CopyFormViewModel
import com.tecsup.visionastra.mobile.ui.resources.ImageUploadScreen
import com.tecsup.visionastra.mobile.ui.resources.ImageUploadViewModel
import com.tecsup.visionastra.mobile.ui.resources.ResourceDetailScreen
import com.tecsup.visionastra.mobile.ui.resources.ResourceDetailViewModel
import com.tecsup.visionastra.mobile.ui.resources.ResourceListScreen
import com.tecsup.visionastra.mobile.ui.resources.ResourceListViewModel
import com.tecsup.visionastra.mobile.ui.resources.ResourceType
import com.tecsup.visionastra.mobile.ui.resources.VideoPlayerScreen
import com.tecsup.visionastra.mobile.ui.resources.displayTitle
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
                    onCampaignsClick = {
                        navController.navigate(AppDestination.Campaigns.route)
                    },
                    onAiGeneratorClick = {
                        navController.navigate(AppDestination.AiGenerations.route)
                    },
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

        composable(AppDestination.Campaigns.route) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.Campaigns.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: CampaignListViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                val message = savedStateHandle?.remove<String>("campaign_message")

                LaunchedEffect(message) {
                    if (message != null) viewModel.showMessage(message)
                }
                OnResumeEffect { viewModel.refresh() }

                CampaignListScreen(
                    state = state,
                    onStatusSelected = viewModel::onStatusSelected,
                    onRetryClick = viewModel::retry,
                    onCreateClick = {
                        navController.navigate(AppDestination.NewCampaign.route)
                    },
                    onCampaignClick = { idCampana ->
                        navController.navigate(AppDestination.CampaignDetail.createRoute(idCampana))
                    },
                    onSnackbarShown = viewModel::onSnackbarShown
                )
            }
        }

        composable(AppDestination.NewCampaign.route) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.NewCampaign.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: CampaignFormViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.savedCampaignId) {
                    val id = state.savedCampaignId
                    if (id != null) {
                        navController.navigate(AppDestination.CampaignDetail.createRoute(id)) {
                            popUpTo(AppDestination.NewCampaign.route) {
                                inclusive = true
                            }
                        }
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("campaign_message", "Campaña creada")
                    }
                }

                CampaignFormScreen(
                    state = state,
                    onNombreChange = viewModel::onNombreChange,
                    onObjetivoChange = viewModel::onObjetivoChange,
                    onDescripcionChange = viewModel::onDescripcionChange,
                    onPresupuestoChange = viewModel::onPresupuestoChange,
                    onFechaInicioSelected = viewModel::onFechaInicioSelected,
                    onFechaFinSelected = viewModel::onFechaFinSelected,
                    onSaveClick = viewModel::save,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppDestination.CampaignDetail.route,
            arguments = listOf(navArgument("idCampana") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.Campaigns.route) { inclusive = false }
                    launchSingleTop = true
                }
            }) {
                val viewModel: CampaignDetailViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                val message = savedStateHandle?.remove<String>("campaign_message")

                LaunchedEffect(message) {
                    if (message != null) viewModel.showMessage(message)
                }
                LaunchedEffect(state.deleted) {
                    if (state.deleted) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("campaign_message", "Campaña eliminada")
                        navController.popBackStack()
                    }
                }
                OnResumeEffect { viewModel.loadCampaign() }

                CampaignDetailScreen(
                    state = state,
                    onRetryClick = viewModel::loadCampaign,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { idCampana ->
                        navController.navigate(AppDestination.EditCampaign.createRoute(idCampana))
                    },
                    onResourcesClick = { idCampana ->
                        navController.navigate(AppDestination.CampaignResources.createRoute(idCampana))
                    },
                    onGenerateAiClick = { idCampana, isActive ->
                        if (isActive) {
                            navController.navigate(AppDestination.NewAiGenerationForCampaign.createRoute(idCampana))
                        }
                    },
                    onStatusChange = viewModel::changeStatus,
                    onDeleteConfirm = viewModel::deleteCampaign,
                    onSnackbarShown = viewModel::onSnackbarShown
                )
            }
        }

        composable(
            route = AppDestination.EditCampaign.route,
            arguments = listOf(navArgument("idCampana") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.Campaigns.route) { inclusive = false }
                    launchSingleTop = true
                }
            }) {
                val viewModel: CampaignFormViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.savedCampaignId) {
                    if (state.savedCampaignId != null) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("campaign_message", "Campaña actualizada")
                        navController.popBackStack()
                    }
                }

                CampaignFormScreen(
                    state = state,
                    onNombreChange = viewModel::onNombreChange,
                    onObjetivoChange = viewModel::onObjetivoChange,
                    onDescripcionChange = viewModel::onDescripcionChange,
                    onPresupuestoChange = viewModel::onPresupuestoChange,
                    onFechaInicioSelected = viewModel::onFechaInicioSelected,
                    onFechaFinSelected = viewModel::onFechaFinSelected,
                    onSaveClick = viewModel::save,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppDestination.CampaignResources.route,
            arguments = listOf(navArgument("idCampana") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.CampaignResources.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: ResourceListViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val idCampana = it.arguments?.getInt("idCampana") ?: return@RequireAuthenticated
                val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                val message = savedStateHandle?.remove<String>("resource_message")

                LaunchedEffect(message) {
                    if (message != null) viewModel.showMessage(message)
                }
                OnResumeEffect { viewModel.loadResources() }

                ResourceListScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onRetryClick = viewModel::loadResources,
                    onTypeSelected = viewModel::onTypeSelected,
                    onUploadImageClick = {
                        navController.navigate(AppDestination.UploadImageResource.createRoute(idCampana))
                    },
                    onCreateCopyClick = {
                        navController.navigate(AppDestination.NewCopyResource.createRoute(idCampana))
                    },
                    onResourceClick = { idRecurso, type ->
                        if (ResourceType.fromValue(type) == ResourceType.Video) {
                            navController.navigate(AppDestination.ResourceVideo.createRoute(idRecurso))
                        } else {
                            navController.navigate(AppDestination.ResourceDetail.createRoute(idRecurso))
                        }
                    },
                    onSnackbarShown = viewModel::onSnackbarShown
                )
            }
        }

        composable(
            route = AppDestination.UploadImageResource.route,
            arguments = listOf(navArgument("idCampana") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.UploadImageResource.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: ImageUploadViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.uploaded) {
                    if (state.uploaded) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("resource_message", "Imagen subida")
                        navController.popBackStack()
                    }
                }

                ImageUploadScreen(
                    state = state,
                    onImageSelected = viewModel::onImageSelected,
                    onTitleChange = viewModel::onTitleChange,
                    onUploadClick = viewModel::upload,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppDestination.NewCopyResource.route,
            arguments = listOf(navArgument("idCampana") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.NewCopyResource.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: CopyFormViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.saved) {
                    if (state.saved) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("resource_message", "Idea creada")
                        navController.popBackStack()
                    }
                }

                CopyFormScreen(
                    state = state,
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = viewModel::onContentChange,
                    onSaveClick = viewModel::save,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppDestination.ResourceDetail.route,
            arguments = listOf(navArgument("idRecurso") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.ResourceDetail.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: ResourceDetailViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.deleted) {
                    if (state.deleted) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("resource_message", "Recurso eliminado")
                        navController.popBackStack()
                    }
                }
                OnResumeEffect { viewModel.loadResource() }

                ResourceDetailScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onRetryClick = viewModel::loadResource,
                    onVideoClick = { idRecurso ->
                        navController.navigate(AppDestination.ResourceVideo.createRoute(idRecurso))
                    },
                    onUpdateTitle = viewModel::updateTitle,
                    onDeleteConfirm = viewModel::deleteResource,
                    onSnackbarShown = viewModel::onSnackbarShown
                )
            }
        }

        composable(
            route = AppDestination.ResourceVideo.route,
            arguments = listOf(navArgument("idRecurso") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.ResourceVideo.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: ResourceDetailViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val resource = state.resource
                val fileUrl = state.fileUrl

                if (resource != null && fileUrl != null) {
                    VideoPlayerScreen(
                        idResource = resource.idRecurso,
                        title = resource.displayTitle(),
                        fileUrl = fileUrl,
                        isDownloading = state.isDownloadingVideo,
                        errorMessage = state.errorMessage,
                        snackbarMessage = state.snackbarMessage,
                        onBackClick = { navController.popBackStack() },
                        onDownloadVideo = viewModel::downloadVideo,
                        onSnackbarShown = viewModel::onSnackbarShown
                    )
                } else {
                    SplashScreen()
                }
            }
        }

        composable(AppDestination.AiGenerations.route) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.AiGenerations.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: AiGenerationListViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                OnResumeEffect { viewModel.load() }

                AiGenerationListScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onNewClick = { navController.navigate(AppDestination.NewAiGeneration.route) },
                    onGenerationClick = { idGeneracion ->
                        navController.navigate(AppDestination.AiGenerationDetail.createRoute(idGeneracion))
                    },
                    onRetryClick = viewModel::load
                )
            }
        }

        composable(AppDestination.NewAiGeneration.route) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.NewAiGeneration.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: AiGenerationFormViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.createdGenerationId) {
                    val id = state.createdGenerationId
                    if (id != null) {
                        navController.navigate(AppDestination.AiGenerationDetail.createRoute(id)) {
                            popUpTo(AppDestination.NewAiGeneration.route) { inclusive = true }
                        }
                    }
                }

                AiGenerationFormScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onCampaignSelected = viewModel::onCampaignSelected,
                    onResourceToggle = viewModel::toggleResource,
                    onPromptChange = viewModel::onPromptChange,
                    onCreateClick = viewModel::createGeneration
                )
            }
        }

        composable(
            route = AppDestination.NewAiGenerationForCampaign.route,
            arguments = listOf(navArgument("idCampana") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.NewAiGenerationForCampaign.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: AiGenerationFormViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.createdGenerationId) {
                    val id = state.createdGenerationId
                    if (id != null) {
                        navController.navigate(AppDestination.AiGenerationDetail.createRoute(id)) {
                            popUpTo(AppDestination.NewAiGenerationForCampaign.route) { inclusive = true }
                        }
                    }
                }

                AiGenerationFormScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onCampaignSelected = viewModel::onCampaignSelected,
                    onResourceToggle = viewModel::toggleResource,
                    onPromptChange = viewModel::onPromptChange,
                    onCreateClick = viewModel::createGeneration
                )
            }
        }

        composable(
            route = AppDestination.AiGenerationDetail.route,
            arguments = listOf(navArgument("idGeneracion") { type = NavType.IntType })
        ) {
            RequireAuthenticated(sessionState, onUnauthenticated = {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.AiGenerationDetail.route) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                val viewModel: AiGenerationDetailViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                AiGenerationDetailScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onCreatePromptClick = viewModel::preparePrompt,
                    onGenerateVideoRequest = viewModel::showGenerateVideoConfirmation,
                    onGenerateVideoConfirm = viewModel::generateVideo,
                    onGenerateVideoCancel = viewModel::dismissGenerateVideoConfirmation,
                    onPlayVideoClick = { idRecurso ->
                        navController.navigate(AppDestination.ResourceVideo.createRoute(idRecurso))
                    }
                )
            }
        }
    }
}

@Composable
private fun RequireAuthenticated(
    sessionState: SessionState,
    onUnauthenticated: () -> Unit,
    content: @Composable () -> Unit
) {
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Unauthenticated) {
            onUnauthenticated()
        }
    }
    if (sessionState is SessionState.Authenticated) {
        content()
    } else {
        SplashScreen()
    }
}

@Composable
private fun OnResumeEffect(
    onResume: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
