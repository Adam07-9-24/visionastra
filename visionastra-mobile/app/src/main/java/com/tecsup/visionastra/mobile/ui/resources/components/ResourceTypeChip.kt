package com.tecsup.visionastra.mobile.ui.resources.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.ui.resources.ResourceType

@Composable
fun ResourceTypeChip(type: String) {
    val resourceType = ResourceType.fromValue(type)
    val colors = resourceTypeColors(resourceType)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.container,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = resourceType.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = colors.content,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class TypeColors(
    val container: Color,
    val content: Color,
    val border: Color
)

private fun resourceTypeColors(type: ResourceType): TypeColors =
    when (type) {
        ResourceType.Image -> TypeColors(Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0xFFBFDBFE))
        ResourceType.Copy -> TypeColors(Color(0xFFF8FAFC), Color(0xFF475569), Color(0xFFDCE5F0))
        ResourceType.Video -> TypeColors(Color(0xFFE0F2FE), Color(0xFF0369A1), Color(0xFFBAE6FD))
        ResourceType.Document -> TypeColors(Color(0xFFFFF7ED), Color(0xFFB45309), Color(0xFFFED7AA))
    }
