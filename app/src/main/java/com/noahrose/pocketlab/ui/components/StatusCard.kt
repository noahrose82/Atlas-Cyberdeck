package com.noahrose.pocketlab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.dashboard.SystemStatus

@Composable
fun StatusCard(
    title: String,
    status: SystemStatus,
    modifier: Modifier = Modifier
) {

    val statusColor =
        statusColor(status)

    Card(
        modifier =
            modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = title,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        status
                            .label
                            .uppercase(),

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,

                    color =
                        statusColor,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Text(
                text =
                    status.symbol,

                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,

                color =
                    statusColor
            )
        }
    }
}

@Composable
private fun statusColor(
    status: SystemStatus
): Color {

    return when (status) {

        SystemStatus.ONLINE,
        SystemStatus.READY,
        SystemStatus.RUNNING -> {

            MaterialTheme
                .colorScheme
                .primary
        }

        SystemStatus.INSTALLING -> {

            MaterialTheme
                .colorScheme
                .tertiary
        }

        SystemStatus.ERROR -> {

            MaterialTheme
                .colorScheme
                .error
        }

        SystemStatus.STOPPED,
        SystemStatus.OFFLINE,
        SystemStatus.NOT_INSTALLED -> {

            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }
    }
}