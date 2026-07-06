package com.tecsup.visionastra.mobile.navigation

sealed class AppDestination(val route: String) {
    data object Splash : AppDestination("splash")
    data object Login : AppDestination("login")
    data object Dashboard : AppDestination("dashboard")
    data object Campaigns : AppDestination("campaigns")
    data object NewCampaign : AppDestination("campaigns/new")
    data object CampaignDetail : AppDestination("campaigns/{idCampana}") {
        fun createRoute(idCampana: Int): String = "campaigns/$idCampana"
    }
    data object EditCampaign : AppDestination("campaigns/{idCampana}/edit") {
        fun createRoute(idCampana: Int): String = "campaigns/$idCampana/edit"
    }
    data object CampaignResources : AppDestination("resources/campaign/{idCampana}") {
        fun createRoute(idCampana: Int): String = "resources/campaign/$idCampana"
    }
    data object UploadImageResource : AppDestination("resources/campaign/{idCampana}/upload-image") {
        fun createRoute(idCampana: Int): String = "resources/campaign/$idCampana/upload-image"
    }
    data object NewCopyResource : AppDestination("resources/campaign/{idCampana}/new-copy") {
        fun createRoute(idCampana: Int): String = "resources/campaign/$idCampana/new-copy"
    }
    data object ResourceDetail : AppDestination("resources/{idRecurso}") {
        fun createRoute(idRecurso: Int): String = "resources/$idRecurso"
    }
    data object ResourceVideo : AppDestination("resources/{idRecurso}/video") {
        fun createRoute(idRecurso: Int): String = "resources/$idRecurso/video"
    }
    data object AiGenerations : AppDestination("ai")
    data object NewAiGeneration : AppDestination("ai/new")
    data object NewAiGenerationForCampaign : AppDestination("ai/new/{idCampana}") {
        fun createRoute(idCampana: Int): String = "ai/new/$idCampana"
    }
    data object AiGenerationDetail : AppDestination("ai/{idGeneracion}") {
        fun createRoute(idGeneracion: Int): String = "ai/$idGeneracion"
    }
}
