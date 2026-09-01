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
 * INTERACTIVE TERMINAL CELL TARGETS
 * ------------------------------------------------
 *
 * Atlas converts the available terminal viewport into
 * rows and columns using a conventional mobile terminal
 * cell target.
 *
 * termlib then renders that geometry into the actual
 * Canvas.
 */
private const val InteractiveTargetCellWidthDp =
    8f

private const val InteractiveTargetCellHeightDp =
    16f

/*
 * ------------------------------------------------
 * PRE-LAUNCH KEY BAR RESERVATION
 * ------------------------------------------------
 *
 * Before nano/vim starts, TerminalScreen is still showing
 * the ordinary command UI.
 *
 * We therefore reserve the height that will be occupied
 * by InteractiveTerminalKeyBar after the PTY launches.
 *
 * InteractiveTerminalKeyBar:
 *
 *     42 dp first row
 *      4 dp row spacing
 *     42 dp second row
 *      8 dp vertical padding
 *
 * Total = 96 dp.
 *
 * Once the interactive screen is visible Atlas no longer
 * estimates this. Scaffold removes the bottom bar and we
 * measure the real remaining terminal viewport directly.
 */
private const val InteractiveKeyBarHeightDp =
    96f

/*
 * Prevent unusual Android layout states from requesting
 * unreasonable terminal dimensions.
 */
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
     * ------------------------------------------------
     * PRE-LAUNCH MEASUREMENT
     * ------------------------------------------------
     *
     * This is the ordinary terminal area measured before
     * nano/vim starts.
     *
     * It gives Atlas a reasonable initial PTY size so the
     * application does not need to begin at a blind 80x24.
     */
    var measuredTerminalArea by
    remember {

        mutableStateOf(
            IntSize.Zero
        )
    }

    /*
     * ------------------------------------------------
     * LIVE INTERACTIVE VIEWPORT
     * ------------------------------------------------
     *
     * Unlike measuredTerminalArea, this represents the
     * real black termlib area after:
     *
     *     Scaffold layout
     *     IME padding
     *     Atlas key bar
     *
     * have already been accounted for.
     *
     * This is the authoritative viewport used for live
     * PTY resizing.
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
     *
     * While there is no active PTY, estimate the geometry
     * that will be needed once the interactive screen
     * replaces the normal terminal.
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
         * The interactive Scaffold will introduce the
         * Atlas mobile terminal key bar after launch.
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
     * LIVE INTERACTIVE GEOMETRY
     * ------------------------------------------------
     *
     * This is the important path.
     *
     * Once nano/vim is active we stop estimating and use
     * the actual termlib viewport.
     *
     * Any Android event that changes that viewport becomes
     * the same operation:
     *
     *     soft keyboard opens
     *     soft keyboard closes
     *     multi-window changes
     *     display/window size changes
     *
     * TerminalViewModel debounces these measurements before
     * asking the persistent controller to resize the PTY.
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

        val viewportWidthDp =
            with(
                density
            ) {

                measuredInteractiveViewport
                    .width
                    .toDp()
                    .value
            }

        val viewportHeightDp =
            with(
                density
            ) {

                measuredInteractiveViewport
                    .height
                    .toDp()
                    .value
            }

        /*
         * Do NOT subtract the key bar here.
         *
         * Scaffold innerPadding has already removed it from
         * this measured viewport.
         */
        val calculatedColumns =
            (
                    viewportWidthDp /
                            InteractiveTargetCellWidthDp
                    )
                .toInt()
                .coerceIn(
                    InteractiveMinimumColumns,
                    InteractiveMaximumColumns
                )

        val calculatedRows =
            (
                    viewportHeightDp /
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
     * INTERACTIVE TERMINAL
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

                InteractiveTerminalKeyBar(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .zIndex(
                                10f
                            ),

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
            }
        ) { innerPadding ->

            /*
             * Outer container applies Scaffold geometry.
             *
             * The nested Box below is therefore exactly the
             * space that belongs to termlib.
             */
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

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .onSizeChanged { size ->

                                measuredInteractiveViewport =
                                    size
                            }
                ) {

                    /*
                     * termlib always receives the geometry
                     * currently confirmed by the PTY stack.
                     *
                     * A viewport change first flows through:
                     *
                     *     TerminalScreen
                     *     TerminalViewModel
                     *     SessionController
                     *     Bridge
                     *     Linux PTY
                     *
                     * Only after successful resize are these
                     * rows and columns updated.
                     */
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

                        keyboardEnabled =
                            true,

                        showSoftKeyboard =
                            true,

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
 * ATLAS MOBILE TERMINAL KEYBOARD
 * ------------------------------------------------
 *
 * Device-validated two-row terminal control surface.
 *
 * Do not replace this with the future full Atlas keyboard
 * until that keyboard has its own build and device
 * validation.
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
                        "→",

                    onClick =
                        onArrowRight
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
            }

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
                        "CANCEL ^C",

                    onClick =
                        onControlC
                )

                TerminalKeyButton(
                    modifier =
                        Modifier
                            .weight(
                                1f
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
                                1f
                            ),

                    label =
                        "EXIT ^X",

                    onClick =
                        onControlX
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
                UbuntuTerminalGreen
        )
    }
}