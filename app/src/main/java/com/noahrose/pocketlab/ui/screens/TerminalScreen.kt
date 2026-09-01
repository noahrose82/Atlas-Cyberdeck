package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyMode
import com.noahrose.pocketlab.feature.terminal.TerminalViewModel
import org.connectbot.terminal.Terminal

private val UbuntuTerminalGreen =
    Color(
        0xFF00FF41
    )

private val SafeModeYellow =
    Color(
        0xFFFFD600
    )

private val RecoveryModeAmber =
    Color(
        0xFFFFA000
    )

/*
 * ------------------------------------------------
 * TERMINAL GEOMETRY
 * ------------------------------------------------
 */

private const val InteractiveTargetCellWidthDp =
    8f

private const val InteractiveTargetCellHeightDp =
    16f

/*
 * Existing two-row control bar:
 *
 *     42 dp row
 *      4 dp spacing
 *     42 dp row
 *      8 dp vertical padding
 *
 * Total = 96 dp.
 *
 * This is used only for PRE-LAUNCH geometry.
 *
 * Once the PTY is running, Atlas measures the real
 * remaining terminal viewport directly.
 */
private const val InteractiveKeyBarHeightDp =
    96f

private const val InteractiveMinimumColumns =
    32

private const val InteractiveMaximumColumns =
    120

private const val InteractiveMinimumRows =
    12

private const val InteractiveMaximumRows =
    60

@Composable
fun TerminalScreen(
    terminalViewModel: TerminalViewModel = viewModel()
) {

    val uiState =
        terminalViewModel
            .uiState

    val linuxShellActive =
        terminalViewModel
            .linuxShellActive

    val interactiveSessionActive =
        terminalViewModel
            .interactiveSessionActive

    val interactiveControlArmed =
        terminalViewModel
            .interactiveControlArmed

    val interactiveTerminalRows =
        terminalViewModel
            .interactiveTerminalRows

    val interactiveTerminalColumns =
        terminalViewModel
            .interactiveTerminalColumns

    val density =
        LocalDensity.current

    /*
     * Normal-terminal viewport used to prepare geometry
     * before nano/vim launches.
     */
    var measuredTerminalArea by
    remember {

        mutableStateOf(
            IntSize.Zero
        )
    }

    /*
     * Actual usable termlib viewport while an interactive
     * PTY application is running.
     *
     * Scaffold has already removed the complete Atlas
     * bottom controls/keyboard before this is measured.
     */
    var measuredInteractiveViewport by
    remember {

        mutableStateOf(
            IntSize.Zero
        )
    }

    val safetySnapshot by
    LinuxRuntimeCircuitBreaker
        .snapshotFlow
        .collectAsState()

    val safetyMode =
        safetySnapshot
            .mode

    val terminalBackground =
        when (
            safetyMode
        ) {

            LinuxRuntimeSafetyMode.SAFE_MODE,
            LinuxRuntimeSafetyMode.RECOVERY_ARMED -> {

                Color.Black
            }

            LinuxRuntimeSafetyMode.NORMAL -> {

                if (
                    linuxShellActive
                ) {

                    Color.Black

                } else {

                    MaterialTheme
                        .colorScheme
                        .background
                }
            }
        }

    val terminalTextColor =
        when (
            safetyMode
        ) {

            LinuxRuntimeSafetyMode.SAFE_MODE -> {

                SafeModeYellow
            }

            LinuxRuntimeSafetyMode.RECOVERY_ARMED -> {

                RecoveryModeAmber
            }

            LinuxRuntimeSafetyMode.NORMAL -> {

                if (
                    linuxShellActive
                ) {

                    UbuntuTerminalGreen

                } else {

                    MaterialTheme
                        .colorScheme
                        .primary
                }
            }
        }

    /*
     * ------------------------------------------------
     * PRE-LAUNCH GEOMETRY
     * ------------------------------------------------
     */

    LaunchedEffect(
        measuredTerminalArea,
        interactiveSessionActive,
        density
    ) {

        if (
            interactiveSessionActive
        ) {

            return@LaunchedEffect
        }

        if (
            measuredTerminalArea.width <= 0 ||
            measuredTerminalArea.height <= 0
        ) {

            return@LaunchedEffect
        }

        val availableWidthDp =
            with(
                density
            ) {

                measuredTerminalArea
                    .width
                    .toDp()
                    .value
            }

        val availableHeightDp =
            with(
                density
            ) {

                measuredTerminalArea
                    .height
                    .toDp()
                    .value
            }

        /*
         * Reserve the existing interactive control bar.
         *
         * The larger Atlas keyboard appears after the PTY
         * begins and live resize immediately corrects the
         * guest geometry to the real viewport.
         */
        val interactiveContentHeightDp =
            (
                    availableHeightDp -
                            InteractiveKeyBarHeightDp
                    )
                .coerceAtLeast(
                    0f
                )

        val calculatedColumns =
            (
                    availableWidthDp /
                            InteractiveTargetCellWidthDp
                    )
                .toInt()
                .coerceIn(
                    InteractiveMinimumColumns,
                    InteractiveMaximumColumns
                )

        val calculatedRows =
            (
                    interactiveContentHeightDp /
                            InteractiveTargetCellHeightDp
                    )
                .toInt()
                .coerceIn(
                    InteractiveMinimumRows,
                    InteractiveMaximumRows
                )

        terminalViewModel
            .updateInteractiveTerminalGeometry(
                columns =
                    calculatedColumns,

                rows =
                    calculatedRows
            )
    }

    /*
     * ------------------------------------------------
     * LIVE INTERACTIVE PTY RESIZE
     * ------------------------------------------------
     *
     * The measured area is already the space remaining
     * after Scaffold removes the Atlas bottom bar and
     * Atlas keyboard.
     *
     * Do NOT subtract their height again here.
     */

    LaunchedEffect(
        measuredInteractiveViewport,
        interactiveSessionActive,
        density
    ) {

        if (
            !interactiveSessionActive
        ) {

            return@LaunchedEffect
        }

        if (
            measuredInteractiveViewport.width <= 0 ||
            measuredInteractiveViewport.height <= 0
        ) {

            return@LaunchedEffect
        }

        val availableWidthDp =
            with(
                density
            ) {

                measuredInteractiveViewport
                    .width
                    .toDp()
                    .value
            }

        val availableHeightDp =
            with(
                density
            ) {

                measuredInteractiveViewport
                    .height
                    .toDp()
                    .value
            }

        val calculatedColumns =
            (
                    availableWidthDp /
                            InteractiveTargetCellWidthDp
                    )
                .toInt()
                .coerceIn(
                    InteractiveMinimumColumns,
                    InteractiveMaximumColumns
                )

        val calculatedRows =
            (
                    availableHeightDp /
                            InteractiveTargetCellHeightDp
                    )
                .toInt()
                .coerceIn(
                    InteractiveMinimumRows,
                    InteractiveMaximumRows
                )

        terminalViewModel
            .updateInteractiveTerminalGeometry(
                columns =
                    calculatedColumns,

                rows =
                    calculatedRows
            )
    }

    /*
     * ------------------------------------------------
     * FAIL-CLOSED INTERACTIVE SESSION
     * ------------------------------------------------
     */

    LaunchedEffect(
        safetyMode,
        interactiveSessionActive
    ) {

        if (
            interactiveSessionActive &&
            safetyMode !=
            LinuxRuntimeSafetyMode.NORMAL
        ) {

            terminalViewModel
                .stopInteractiveSession()
        }
    }

    /*
     * ------------------------------------------------
     * INTERACTIVE UBUNTU TERMINAL
     * ------------------------------------------------
     */

    if (
        interactiveSessionActive
    ) {

        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black
                    )
                    .imePadding(),

            containerColor =
                Color.Black,

            bottomBar = {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .zIndex(
                                10f
                            )
                ) {

                    /*
                     * Existing validated Atlas terminal
                     * control/navigation bar.
                     */
                    InteractiveTerminalKeyBar(
                        modifier =
                            Modifier
                                .fillMaxWidth(),

                        controlArmed =
                            interactiveControlArmed,

                        onControl = {

                            terminalViewModel
                                .toggleInteractiveControl()
                        },

                        onEscape = {

                            terminalViewModel
                                .sendInteractiveEscape()
                        },

                        onTab = {

                            terminalViewModel
                                .sendInteractiveTab()
                        },

                        onArrowLeft = {

                            terminalViewModel
                                .sendInteractiveArrowLeft()
                        },

                        onArrowUp = {

                            terminalViewModel
                                .sendInteractiveArrowUp()
                        },

                        onArrowDown = {

                            terminalViewModel
                                .sendInteractiveArrowDown()
                        },

                        onArrowRight = {

                            terminalViewModel
                                .sendInteractiveArrowRight()
                        },

                        onEnter = {

                            terminalViewModel
                                .sendInteractiveEnter()
                        },

                        onBackspace = {

                            terminalViewModel
                                .sendInteractiveBackspace()
                        },

                        onControlC = {

                            terminalViewModel
                                .sendInteractiveControl(
                                    'C'
                                )
                        },

                        onControlO = {

                            terminalViewModel
                                .sendInteractiveControl(
                                    'O'
                                )
                        },

                        onControlX = {

                            terminalViewModel
                                .sendInteractiveControl(
                                    'X'
                                )
                        }
                    )

                    /*
                     * Atlas-native terminal keyboard.
                     *
                     * Android's soft keyboard is no longer
                     * required for normal interactive input.
                     */
                    AtlasTerminalKeyboard(
                        modifier =
                            Modifier
                                .fillMaxWidth(),

                        onText = { text ->

                            terminalViewModel
                                .sendInteractiveText(
                                    text
                                )
                        },

                        onEnter = {

                            terminalViewModel
                                .sendInteractiveEnter()
                        },

                        onBackspace = {

                            terminalViewModel
                                .sendInteractiveBackspace()
                        }
                    )
                }
            }
        ) { innerPadding ->

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            innerPadding
                        )
                        .background(
                            Color.Black
                        )
            ) {

                /*
                 * This Box represents the REAL interactive
                 * terminal viewport.
                 *
                 * Any change here drives the PTY resize
                 * pipeline:
                 *
                 * TerminalScreen
                 *      ↓
                 * TerminalViewModel
                 *      ↓
                 * SessionController
                 *      ↓
                 * Bridge
                 *      ↓
                 * Linux PTY / stty
                 *      ↓
                 * termlib
                 */
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .onSizeChanged { size ->

                                measuredInteractiveViewport =
                                    size
                            }
                ) {

                    Terminal(
                        terminalEmulator =
                            terminalViewModel
                                .interactiveTerminalEmulator,

                        modifier =
                            Modifier
                                .fillMaxSize(),

                        backgroundColor =
                            Color.Black,

                        foregroundColor =
                            UbuntuTerminalGreen,

                        /*
                         * Keep hardware/Bluetooth keyboard
                         * input supported by termlib.
                         */
                        keyboardEnabled =
                            true,

                        /*
                         * Atlas now owns the on-screen
                         * terminal keyboard.
                         */
                        showSoftKeyboard =
                            false,

                        forcedSize =
                            Pair(
                                interactiveTerminalRows,
                                interactiveTerminalColumns
                            )
                    )
                }
            }
        }

        return
    }

    /*
     * ------------------------------------------------
     * NORMAL ATLAS / UBUNTU TERMINAL
     * ------------------------------------------------
     */

    val prompt =
        terminalViewModel
            .prompt

    val listState =
        rememberLazyListState()

    LaunchedEffect(
        uiState.output.size
    ) {

        if (
            uiState
                .output
                .isNotEmpty()
        ) {

            listState
                .animateScrollToItem(
                    uiState.output.lastIndex
                )
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    color =
                        terminalBackground
                )
                .imePadding()
                .onSizeChanged { size ->

                    measuredTerminalArea =
                        size
                }
                .padding(
                    all =
                        16.dp
                )
    ) {

        LazyColumn(
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .fillMaxWidth(),

            state =
                listState,

            verticalArrangement =
                Arrangement.spacedBy(
                    2.dp
                )
        ) {

            items(
                items =
                    uiState.output
            ) { line ->

                Text(
                    text =
                        line,

                    color =
                        terminalTextColor
                )
            }
        }

        if (
            terminalViewModel
                .commandRunning
        ) {

            Text(
                text =
                    "Running...",

                color =
                    terminalTextColor,

                modifier =
                    Modifier
                        .padding(
                            top =
                                6.dp
                        )
            )
        }

        BasicTextField(
            value =
                uiState.currentCommand,

            onValueChange = { value ->

                if (
                    value.contains(
                        "\n"
                    )
                ) {

                    terminalViewModel
                        .executeCommand()

                } else {

                    terminalViewModel
                        .updateCommand(
                            value
                        )
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            8.dp
                    )
                    .onPreviewKeyEvent { keyEvent ->

                        if (
                            keyEvent.type ==
                            KeyEventType.KeyDown
                        ) {

                            when (
                                keyEvent.key
                            ) {

                                Key.Tab -> {

                                    terminalViewModel
                                        .completeCommand()

                                    true
                                }

                                Key.Enter -> {

                                    terminalViewModel
                                        .executeCommand()

                                    true
                                }

                                else -> {

                                    false
                                }
                            }

                        } else {

                            false
                        }
                    },

            singleLine =
                true,

            keyboardOptions =
                KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.None,

                    autoCorrectEnabled =
                        false,

                    keyboardType =
                        KeyboardType.Ascii,

                    imeAction =
                        ImeAction.Done
                ),

            keyboardActions =
                KeyboardActions(
                    onDone = {

                        terminalViewModel
                            .executeCommand()
                    }
                ),

            textStyle =
                TextStyle(
                    color =
                        terminalTextColor
                ),

            cursorBrush =
                SolidColor(
                    terminalTextColor
                ),

            decorationBox = { innerTextField ->

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                ) {

                    Text(
                        text =
                            "$prompt ",

                        color =
                            terminalTextColor
                    )

                    Box(
                        modifier =
                            Modifier
                                .weight(
                                    1f
                                )
                    ) {

                        innerTextField()
                    }
                }
            }
        )
    }
}

/*
 * ------------------------------------------------
 * ATLAS INTERACTIVE CONTROL BAR
 * ------------------------------------------------
 *
 * Row 1:
 *
 * CTRL  ESC  TAB  ←  ↑  ↓  →
 *
 * Row 2:
 *
 * CANCEL ^C  SAVE ^O  EXIT ^X  ENT  BKSP
 */

@Composable
private fun InteractiveTerminalKeyBar(
    modifier: Modifier = Modifier,
    controlArmed: Boolean,
    onControl: () -> Unit,
    onEscape: () -> Unit,
    onTab: () -> Unit,
    onArrowLeft: () -> Unit,
    onArrowUp: () -> Unit,
    onArrowDown: () -> Unit,
    onArrowRight: () -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit,
    onControlC: () -> Unit,
    onControlO: () -> Unit,
    onControlX: () -> Unit
) {

    Surface(
        modifier =
            modifier,

        color =
            Color.Black,

        tonalElevation =
            4.dp
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            4.dp,

                        vertical =
                            4.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    4.dp
                )
        ) {

            /*
             * NAVIGATION / MODIFIER ROW
             */
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        4.dp
                    )
            ) {

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        if (
                            controlArmed
                        ) {

                            "CTRL*"

                        } else {

                            "CTRL"
                        },

                    onClick =
                        onControl
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        "ESC",

                    onClick =
                        onEscape
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        "TAB",

                    onClick =
                        onTab
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        "←",

                    onClick =
                        onArrowLeft
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        "↑",

                    onClick =
                        onArrowUp
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        "↓",

                    onClick =
                        onArrowDown
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        "→",

                    onClick =
                        onArrowRight
                )
            }

            /*
             * INTERACTIVE APPLICATION ACTION ROW
             */
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        4.dp
                    )
            ) {

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1.35f
                            ),

                    label =
                        "CANCEL ^C",

                    onClick =
                        onControlC
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1.2f
                            ),

                    label =
                        "SAVE ^O",

                    onClick =
                        onControlO
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1.15f
                            ),

                    label =
                        "EXIT ^X",

                    onClick =
                        onControlX
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                0.8f
                            ),

                    label =
                        "ENT",

                    onClick =
                        onEnter
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        "BKSP",

                    onClick =
                        onBackspace
                )
            }
        }
    }
}

@Composable
private fun TerminalKeyButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit
) {

    OutlinedButton(
        modifier =
            modifier
                .height(
                    42.dp
                ),

        onClick =
            onClick,

        contentPadding =
            PaddingValues(
                horizontal =
                    2.dp,

                vertical =
                    0.dp
            )
    ) {

        Text(
            text =
                label,

            color =
                UbuntuTerminalGreen,

            maxLines =
                1
        )
    }
}