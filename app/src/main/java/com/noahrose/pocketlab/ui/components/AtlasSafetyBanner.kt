package com.noahrose.pocketlab.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyMode
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetySnapshot

private val SafeModeYellow =
    Color(
        0xFFFFD600
    )

private val RecoveryModeAmber =
    Color(
        0xFFFFA000
    )

@Composable
fun AtlasSafetyBanner(
    snapshot: LinuxRuntimeSafetySnapshot
) {

    if (
        snapshot.mode ==
        LinuxRuntimeSafetyMode.NORMAL
    ) {

        return
    }

    val accentColor =
        when (
            snapshot.mode
        ) {

            LinuxRuntimeSafetyMode.SAFE_MODE ->
                SafeModeYellow

            LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                RecoveryModeAmber

            LinuxRuntimeSafetyMode.NORMAL ->
                return
        }

    val title =
        when (
            snapshot.mode
        ) {

            LinuxRuntimeSafetyMode.SAFE_MODE ->
                "ATLAS SAFE MODE"

            LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                "ATLAS RECOVERY MODE"

            LinuxRuntimeSafetyMode.NORMAL ->
                ""
        }

    val status =
        when (
            snapshot.mode
        ) {

            LinuxRuntimeSafetyMode.SAFE_MODE ->
                "Linux runtime blocked • Open Terminal and run: safety recover"

            LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                "Linux runtime restricted to recovery operations"

            LinuxRuntimeSafetyMode.NORMAL ->
                ""
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth(),

        color =
            Color.Black,

        contentColor =
            accentColor
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            16.dp,

                        vertical =
                            10.dp
                    )
        ) {

            Text(
                text =
                    title,

                color =
                    accentColor,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    status,

                color =
                    accentColor
            )

            snapshot
                .reason
                ?.let { reason ->

                    Text(
                        text =
                            "Reason: $reason",

                        color =
                            accentColor
                    )
                }
        }
    }
}
