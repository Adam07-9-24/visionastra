package com.tecsup.visionastra.mobile.ui.campaigns.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tecsup.visionastra.mobile.ui.campaigns.CampaignStatus

@Composable
fun CampaignStatusChip(
    status: String
) {
    val campaignStatus = CampaignStatus.fromValue(status)
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = campaignStatus.label,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}
