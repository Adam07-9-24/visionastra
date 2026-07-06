package com.tecsup.visionastra.mobile.ui.resources

import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.tecsup.visionastra.mobile.core.network.AuthenticatedImageLoaderEntryPoint
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    idResource: Int,
    title: String,
    fileUrl: String,
    isDownloading: Boolean,
    errorMessage: String?,
    snackbarMessage: String?,
    onBackClick: () -> Unit,
    onDownloadVideo: (Uri) -> Unit,
    onSnackbarShown: () -> Unit
) {
    val colors = VideoPalette()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isChoosingDestination by remember { mutableStateOf(false) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { uri ->
        isChoosingDestination = false
        if (uri != null) {
            onDownloadVideo(uri)
        }
    }
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, AuthenticatedImageLoaderEntryPoint::class.java)
    }
    val player = remember(fileUrl) {
        val dataSourceFactory = OkHttpDataSource.Factory(entryPoint.okHttpClient())
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(fileUrl))
        ExoPlayer.Builder(context).build().apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Reproducir video",
                subtitle = "Contenido generado con VisionAstra",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .aspectRatio(9f / 16f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            PlayerView(it).apply {
                                this.player = player
                                useController = true
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                setBackgroundColor(android.graphics.Color.BLACK)
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        update = {
                            it.player = player
                            it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("Video generado con VisionAstra", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }
            Button(
                onClick = {
                    if (!isDownloading && !isChoosingDestination) {
                        isChoosingDestination = true
                        createDocumentLauncher.launch("VisionAstra_$idResource.mp4")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                enabled = !isDownloading && !isChoosingDestination,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.sky)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Text("Descargando...", modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.SemiBold)
                    } else {
                        DownloadGlyph(Color.White)
                        Text(
                            if (isChoosingDestination) "Seleccionando destino..." else "Descargar video",
                            modifier = Modifier.padding(start = 10.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            errorMessage?.let {
                Text(it, color = colors.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun BackGlyph(color: Color) {
    Canvas(Modifier.size(24.dp)) {
        drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx())
        drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx())
        drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx())
    }
}

@Composable
private fun DownloadGlyph(color: Color) {
    Canvas(Modifier.size(20.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(10.dp.toPx(), 3.dp.toPx()), Offset(10.dp.toPx(), 12.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(6.dp.toPx(), 9.dp.toPx()), Offset(10.dp.toPx(), 13.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(14.dp.toPx(), 9.dp.toPx()), Offset(10.dp.toPx(), 13.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(
            color = color,
            topLeft = Offset(4.dp.toPx(), 15.dp.toPx()),
            size = Size(12.dp.toPx(), 2.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()),
            style = stroke
        )
    }
}

private data class VideoPalette(
    val background: Color = Color(0xFFF8FAFC),
    val surface: Color = Color.White,
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val sky: Color = Color(0xFF0EA5E9),
    val border: Color = Color(0xFFD7E3F0),
    val error: Color = Color(0xFFB42318)
)
