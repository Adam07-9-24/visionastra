package com.tecsup.visionastra.mobile.ui.resources.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.tecsup.visionastra.mobile.core.network.NetworkConstants
import com.tecsup.visionastra.mobile.core.network.authenticatedImageLoader
import com.tecsup.visionastra.mobile.core.util.formatMb
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse
import com.tecsup.visionastra.mobile.ui.resources.ResourceType
import com.tecsup.visionastra.mobile.ui.resources.displayTitle
import com.tecsup.visionastra.mobile.ui.resources.formatResourceDate

@Composable
fun ResourceCard(
    resource: ResourceResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = resource.displayTitle(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                ResourceTypeChip(type = resource.tipo)
            }
            when (ResourceType.fromValue(resource.tipo)) {
                ResourceType.Copy -> Text(
                    text = resource.contenidoTexto.orEmpty().ifBlank { "Sin contenido" },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ResourceType.Document -> Text(
                    text = "Documento no disponible en la aplicación móvil",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ResourceType.Video -> Text(
                    text = "Video existente · toca para reproducir",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ResourceType.Image -> {
                    val context = LocalContext.current
                    AsyncImage(
                        model = "${NetworkConstants.BASE_URL}api/recursos/archivo/${resource.idRecurso}",
                        imageLoader = remember { authenticatedImageLoader(context) },
                        contentDescription = resource.displayTitle(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }
            Text(
                text = "Estado: ${resource.estado} · ${resource.pesoMb.formatMb()}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Subido: ${resource.fechaSubida.formatResourceDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
