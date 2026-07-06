package com.tecsup.visionastra.mobile.ui.resources.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tecsup.visionastra.mobile.ui.resources.ResourceType

@Composable
fun ResourceTypeChip(type: String) {
    AssistChip(
        onClick = {},
        label = { Text(ResourceType.fromValue(type).label) }
    )
}
