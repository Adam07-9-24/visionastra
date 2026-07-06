package com.tecsup.visionastra.mobile.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.core.session.AuthUser

@Composable
fun DashboardScreen(
    user: AuthUser,
    isLoggingOut: Boolean,
    onCampaignsClick: () -> Unit,
    onAiGeneratorClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = dashboardPalette
    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DashboardHeader(colors = colors)
            WelcomeCard(
                firstName = user.nombres.firstName(),
                colors = colors
            )
            Text(
                text = "Acciones principales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            DashboardActionCard(
                title = "Campanas",
                description = "Crea y administra tus campanas de marketing.",
                colors = colors,
                onClick = onCampaignsClick,
                icon = { CampaignGlyph(colors.primary) }
            )
            DashboardActionCard(
                title = "Generador IA",
                description = "Convierte tus ideas en contenido listo para publicar.",
                colors = colors,
                onClick = onAiGeneratorClick,
                icon = { SparkGlyph(colors.sky) }
            )
            CreativeSpaceCard(colors = colors)
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoggingOut,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.logoutSoft,
                    contentColor = colors.logout,
                    disabledContentColor = colors.logout.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, colors.logout.copy(alpha = 0.28f))
            ) {
                LogoutGlyph(colors.logout)
                Text(
                    text = if (isLoggingOut) "Cerrando sesion..." else "Cerrar sesion",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DashboardHeader(colors: DashboardPalette) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.primary, colors.sky))),
            contentAlignment = Alignment.Center
        ) {
            MiniMark()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "VisionAstra",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Marketing con inteligencia artificial",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WelcomeCard(
    firstName: String,
    colors: DashboardPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.soft,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(listOf(colors.border, colors.sky.copy(alpha = 0.38f)))
        ),
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hola, $firstName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Gestiona tus campanas y contenido con IA.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun CreativeSpaceCard(colors: DashboardPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.skySoft,
        border = BorderStroke(1.dp, colors.sky.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    SparkGlyph(colors.sky)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu espacio creativo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Organiza campanas, prepara recursos y genera contenido con inteligencia artificial.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniPill("Campanas", colors)
                MiniPill("Recursos", colors)
                MiniPill("Contenido IA", colors)
            }
        }
    }
}

@Composable
private fun MiniPill(text: String, colors: DashboardPalette) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    description: String,
    colors: DashboardPalette,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.soft),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            ArrowGlyph(colors.sky)
        }
    }
}

private fun String.firstName(): String =
    trim().split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotBlank() } ?: "Usuario"

@Composable
private fun MiniMark() {
    Canvas(modifier = Modifier.size(22.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val path = Path().apply {
            moveTo(cx, 1.dp.toPx())
            lineTo(cx + 4.dp.toPx(), cy - 4.dp.toPx())
            lineTo(size.width - 1.dp.toPx(), cy)
            lineTo(cx + 4.dp.toPx(), cy + 4.dp.toPx())
            lineTo(cx, size.height - 1.dp.toPx())
            lineTo(cx - 4.dp.toPx(), cy + 4.dp.toPx())
            lineTo(1.dp.toPx(), cy)
            lineTo(cx - 4.dp.toPx(), cy - 4.dp.toPx())
            close()
        }
        drawPath(path, Color.White)
        drawCircle(Color.White.copy(alpha = 0.72f), 1.5.dp.toPx(), Offset(cx, cy))
    }
}

@Composable
private fun CampaignGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(
            color = color,
            topLeft = Offset(3.dp.toPx(), 5.dp.toPx()),
            size = Size(18.dp.toPx(), 14.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = stroke
        )
        drawLine(color, Offset(7.dp.toPx(), 10.dp.toPx()), Offset(17.dp.toPx(), 10.dp.toPx()), 1.7.dp.toPx())
        drawLine(color, Offset(7.dp.toPx(), 14.dp.toPx()), Offset(14.dp.toPx(), 14.dp.toPx()), 1.7.dp.toPx())
    }
}

@Composable
private fun SparkGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(12.dp.toPx(), 3.dp.toPx()), Offset(12.dp.toPx(), 9.dp.toPx()), 1.9.dp.toPx())
        drawLine(color, Offset(12.dp.toPx(), 15.dp.toPx()), Offset(12.dp.toPx(), 21.dp.toPx()), 1.9.dp.toPx())
        drawLine(color, Offset(3.dp.toPx(), 12.dp.toPx()), Offset(9.dp.toPx(), 12.dp.toPx()), 1.9.dp.toPx())
        drawLine(color, Offset(15.dp.toPx(), 12.dp.toPx()), Offset(21.dp.toPx(), 12.dp.toPx()), 1.9.dp.toPx())
        drawCircle(color.copy(alpha = 0.18f), 6.dp.toPx(), Offset(12.dp.toPx(), 12.dp.toPx()), style = stroke)
    }
}

@Composable
private fun ArrowGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(4.dp.toPx(), 9.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(10.dp.toPx(), 5.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(10.dp.toPx(), 13.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
    }
}

@Composable
private fun LogoutGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(7.dp.toPx(), 4.dp.toPx()), Offset(3.dp.toPx(), 9.dp.toPx()), 1.8.dp.toPx())
        drawLine(color, Offset(3.dp.toPx(), 9.dp.toPx()), Offset(7.dp.toPx(), 14.dp.toPx()), 1.8.dp.toPx())
        drawLine(color, Offset(3.dp.toPx(), 9.dp.toPx()), Offset(13.dp.toPx(), 9.dp.toPx()), 1.8.dp.toPx())
        drawArc(
            color = color.copy(alpha = 0.55f),
            startAngle = -70f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(7.dp.toPx(), 3.dp.toPx()),
            size = Size(8.dp.toPx(), 12.dp.toPx()),
            style = stroke
        )
    }
}

@Immutable
private data class DashboardPalette(
    val background: Color = Color(0xFFF8FAFC),
    val surface: Color = Color.White,
    val soft: Color = Color(0xFFEFF6FF),
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val primary: Color = Color(0xFF3B82F6),
    val sky: Color = Color(0xFF0EA5E9),
    val cyan: Color = Color(0xFF38BDF8),
    val skySoft: Color = Color(0xFFE0F2FE),
    val border: Color = Color(0xFFD7E3F0),
    val logout: Color = Color(0xFFEF4444),
    val logoutSoft: Color = Color(0xFFFEF2F2)
)

private val dashboardPalette = DashboardPalette()
