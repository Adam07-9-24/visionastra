package com.tecsup.visionastra.mobile.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = lightLoginPalette
    val fieldShape = RoundedCornerShape(18.dp)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            colors.backgroundSoft
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FuturisticHeader(colors = colors)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "VisionAstra",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Marketing inteligente, resultados extraordinarios",
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = colors.card,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                colors.border,
                                colors.indigo.copy(alpha = 0.22f)
                            )
                        )
                    ),
                    shadowElevation = 10.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Bienvenido de nuevo",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Inicia sesion para administrar tus campanas y contenido con IA.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Correo electronico") },
                            placeholder = { Text("nombre@empresa.com") },
                            leadingIcon = { MailGlyph(color = colors.textSecondary) },
                            singleLine = true,
                            enabled = !state.isLoading,
                            shape = fieldShape,
                            colors = loginTextFieldColors(colors),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Contrasena") },
                            placeholder = { Text("Tu contrasena") },
                            leadingIcon = { LockGlyph(color = colors.textSecondary) },
                            trailingIcon = {
                                IconButton(
                                    onClick = onPasswordVisibilityChange,
                                    enabled = !state.isLoading
                                ) {
                                    if (state.passwordVisible) {
                                        EyeOffGlyph(color = colors.textSecondary)
                                    } else {
                                        EyeGlyph(color = colors.textSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            enabled = !state.isLoading,
                            shape = fieldShape,
                            visualTransformation = if (state.passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            colors = loginTextFieldColors(colors),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { if (!state.isLoading) onLoginClick() }
                            )
                        )
                        state.errorMessage?.let {
                            ErrorMessage(message = it, colors = colors)
                        }
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.indigo,
                                contentColor = Color.White,
                                disabledContainerColor = colors.indigo.copy(alpha = 0.55f),
                                disabledContentColor = Color.White.copy(alpha = 0.85f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 1.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            if (state.isLoading) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Ingresando...")
                                }
                            } else {
                                Text(
                                    text = "Iniciar sesion",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShieldGlyph(color = colors.cyan)
                            Text(
                                text = "Acceso seguro protegido por VisionAstra",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun FuturisticHeader(colors: LoginPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            val center = Offset(size.width / 2f, size.height * 0.53f)
            drawArc(
                color = colors.indigo.copy(alpha = 0.34f),
                startAngle = -18f,
                sweepAngle = 252f,
                useCenter = false,
                topLeft = Offset(center.x - 74.dp.toPx(), center.y - 34.dp.toPx()),
                size = Size(148.dp.toPx(), 68.dp.toPx()),
                style = stroke
            )
            drawArc(
                color = colors.cyan.copy(alpha = 0.46f),
                startAngle = 170f,
                sweepAngle = 122f,
                useCenter = false,
                topLeft = Offset(center.x - 86.dp.toPx(), center.y - 42.dp.toPx()),
                size = Size(172.dp.toPx(), 84.dp.toPx()),
                style = stroke
            )
            val nodes = listOf(
                Offset(center.x - 68.dp.toPx(), center.y - 16.dp.toPx()),
                Offset(center.x - 34.dp.toPx(), center.y + 24.dp.toPx()),
                Offset(center.x + 54.dp.toPx(), center.y - 26.dp.toPx()),
                Offset(center.x + 76.dp.toPx(), center.y + 12.dp.toPx())
            )
            drawLine(colors.sky.copy(alpha = 0.30f), nodes[0], nodes[1], 1.2.dp.toPx())
            drawLine(colors.cyan.copy(alpha = 0.34f), nodes[1], nodes[2], 1.2.dp.toPx())
            drawLine(colors.indigo.copy(alpha = 0.34f), nodes[2], nodes[3], 1.2.dp.toPx())
            nodes.forEachIndexed { index, node ->
                drawCircle(
                    color = if (index % 2 == 0) colors.cyan else colors.sky,
                    radius = 2.8.dp.toPx(),
                    center = node
                )
            }
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(colors.indigo, colors.sky, colors.cyan)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            VisionAstraMark()
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    colors: LoginPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WarningGlyph(color = colors.errorText)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.errorText
            )
        }
    }
}

@Composable
private fun loginTextFieldColors(colors: LoginPalette) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = colors.indigo,
    unfocusedBorderColor = colors.border,
    focusedLabelColor = colors.indigo,
    cursorColor = colors.indigo,
    focusedContainerColor = colors.field,
    unfocusedContainerColor = colors.field,
    disabledContainerColor = colors.field.copy(alpha = 0.58f),
    focusedTextColor = colors.textPrimary,
    unfocusedTextColor = colors.textPrimary,
    focusedPlaceholderColor = colors.textSecondary.copy(alpha = 0.62f),
    unfocusedPlaceholderColor = colors.textSecondary.copy(alpha = 0.62f)
)

@Composable
private fun VisionAstraMark() {
    Canvas(modifier = Modifier.size(44.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val path = Path().apply {
            moveTo(cx, 2.dp.toPx())
            lineTo(cx + 8.dp.toPx(), cy - 8.dp.toPx())
            lineTo(size.width - 2.dp.toPx(), cy)
            lineTo(cx + 8.dp.toPx(), cy + 8.dp.toPx())
            lineTo(cx, size.height - 2.dp.toPx())
            lineTo(cx - 8.dp.toPx(), cy + 8.dp.toPx())
            lineTo(2.dp.toPx(), cy)
            lineTo(cx - 8.dp.toPx(), cy - 8.dp.toPx())
            close()
        }
        drawPath(path, Color.White.copy(alpha = 0.96f))
        drawCircle(Color.White.copy(alpha = 0.72f), radius = 3.dp.toPx(), center = Offset(cx, cy))
    }
}

@Composable
private fun MailGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val left = 2.dp.toPx()
        val top = 5.dp.toPx()
        val right = size.width - 2.dp.toPx()
        val bottom = size.height - 5.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = stroke
        )
        drawLine(color, Offset(left + 2.dp.toPx(), top + 2.dp.toPx()), Offset(size.width / 2f, size.height / 2f), 1.6.dp.toPx())
        drawLine(color, Offset(right - 2.dp.toPx(), top + 2.dp.toPx()), Offset(size.width / 2f, size.height / 2f), 1.6.dp.toPx())
    }
}

@Composable
private fun LockGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = color,
            startAngle = 198f,
            sweepAngle = 144f,
            useCenter = false,
            topLeft = Offset(5.dp.toPx(), 2.dp.toPx()),
            size = Size(12.dp.toPx(), 13.dp.toPx()),
            style = stroke
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(4.dp.toPx(), 10.dp.toPx()),
            size = Size(14.dp.toPx(), 10.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = stroke
        )
        drawCircle(color, radius = 1.3.dp.toPx(), center = Offset(size.width / 2f, 15.dp.toPx()))
    }
}

@Composable
private fun EyeGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(2.dp.toPx(), 5.dp.toPx()),
            size = Size(18.dp.toPx(), 12.dp.toPx()),
            style = stroke
        )
        drawArc(
            color = color,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(2.dp.toPx(), 5.dp.toPx()),
            size = Size(18.dp.toPx(), 12.dp.toPx()),
            style = stroke
        )
        drawCircle(color, radius = 2.4.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
    }
}

@Composable
private fun EyeOffGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(4.dp.toPx(), 18.dp.toPx()), Offset(18.dp.toPx(), 4.dp.toPx()), 1.9.dp.toPx())
        drawArc(
            color = color,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(2.dp.toPx(), 5.dp.toPx()),
            size = Size(18.dp.toPx(), 12.dp.toPx()),
            style = stroke
        )
        drawArc(
            color = color,
            startAngle = 25f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(2.dp.toPx(), 5.dp.toPx()),
            size = Size(18.dp.toPx(), 12.dp.toPx()),
            style = stroke
        )
    }
}

@Composable
private fun ShieldGlyph(color: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width / 2f, 1.dp.toPx())
            lineTo(size.width - 2.dp.toPx(), 4.dp.toPx())
            lineTo(size.width - 4.dp.toPx(), 11.dp.toPx())
            lineTo(size.width / 2f, size.height - 1.dp.toPx())
            lineTo(4.dp.toPx(), 11.dp.toPx())
            lineTo(2.dp.toPx(), 4.dp.toPx())
            close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun WarningGlyph(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width / 2f, 2.dp.toPx())
            lineTo(size.width - 2.dp.toPx(), size.height - 3.dp.toPx())
            lineTo(2.dp.toPx(), size.height - 3.dp.toPx())
            close()
        }
        drawPath(path, color, style = stroke)
        drawLine(color, Offset(size.width / 2f, 7.dp.toPx()), Offset(size.width / 2f, 12.dp.toPx()), 1.8.dp.toPx())
        drawCircle(color, radius = 1.dp.toPx(), center = Offset(size.width / 2f, 15.dp.toPx()))
    }
}

@Immutable
private data class LoginPalette(
    val background: Color,
    val backgroundSoft: Color,
    val card: Color,
    val field: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val indigo: Color,
    val sky: Color,
    val cyan: Color,
    val border: Color,
    val errorContainer: Color,
    val errorText: Color
)

private val lightLoginPalette = LoginPalette(
    background = Color(0xFFF8FAFC),
    backgroundSoft = Color(0xFFFFFFFF),
    card = Color.White,
    field = Color.White,
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    indigo = Color(0xFF3B82F6),
    sky = Color(0xFF0EA5E9),
    cyan = Color(0xFF38BDF8),
    border = Color(0xFFDCE5F0),
    errorContainer = Color(0xFFFFEEF2),
    errorText = Color(0xFF991B1B)
)
