package com.tecsup.visionastra.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tecsup.visionastra.mobile.core.session.SessionManager
import com.tecsup.visionastra.mobile.navigation.VisionAstraNavHost
import com.tecsup.visionastra.mobile.ui.theme.VisionAstraMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisionAstraMobileTheme {
                VisionAstraNavHost(sessionManager = sessionManager)
            }
        }
    }
}
