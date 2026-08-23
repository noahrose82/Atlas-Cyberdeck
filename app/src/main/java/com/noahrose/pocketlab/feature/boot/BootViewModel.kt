package com.noahrose.pocketlab.feature.boot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BootViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            BootUiState()
        )

    val uiState: StateFlow<BootUiState> =
        _uiState.asStateFlow()

    init {

        beginBootSequence()
    }

    private fun beginBootSequence() {

        viewModelScope.launch {

            /*
             * Retrieve the capability state that
             * was created during Atlas bootstrap.
             */
            val capabilities =
                DeviceBootstrapManager
                    .getCapabilities()

            val deviceReady =
                capabilities
                    ?.overallReady
                    ?: false

            val coreSteps =
                listOf(
                    BootStep.STARTING,
                    BootStep.WORKSPACE,
                    BootStep.DEVICE_PROFILE,
                    BootStep.CAPABILITIES,
                    BootStep.LINUX,
                    BootStep.TERMINAL,
                    BootStep.SSH,
                    BootStep.FILE_SYSTEM
                )

            val finalStep =
                if (deviceReady) {

                    BootStep.READY

                } else {

                    BootStep.DEGRADED
                }

            val steps =
                coreSteps +
                        finalStep

            steps.forEachIndexed {
                    index,
                    step ->

                val isFinalStep =
                    step == BootStep.READY ||
                            step == BootStep.DEGRADED

                /*
                 * Do not expose the capability
                 * results before Atlas reaches
                 * the capability analysis step.
                 */
                val visibleCapabilities =
                    if (
                        index >=
                        coreSteps.indexOf(
                            BootStep.CAPABILITIES
                        )
                    ) {

                        capabilities

                    } else {

                        null
                    }

                _uiState.value =
                    BootUiState(

                        currentStep =
                            step,

                        progress =
                            (index + 1)
                                .toFloat() /
                                    steps.size,

                        isComplete =
                            isFinalStep,

                        isDegraded =
                            step ==
                                    BootStep.DEGRADED,

                        capabilities =
                            visibleCapabilities
                    )

                delay(
                    timeMillis =
                        if (isFinalStep) {

                            1200L

                        } else {

                            550L
                        }
                )
            }
        }
    }
}