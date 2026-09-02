package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AtlasKeyboardGreen =
    Color(
        0xFF00FF41
    )

@Composable
fun AtlasTerminalKeyboard(
    modifier: Modifier = Modifier,
    onText: (String) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit
) {

    var shiftActive by
    remember {
        mutableStateOf(
            false
        )
    }

    var symbolMode by
    remember {
        mutableStateOf(
            false
        )
    }

    fun sendLetter(
        character: Char
    ) {

        val output =
            if (
                shiftActive
            ) {

                character
                    .uppercaseChar()
                    .toString()

            } else {

                character
                    .toString()
            }

        onText(
            output
        )

        /*
         * SHIFT is one-shot.
         */
        shiftActive =
            false
    }

    fun sendSymbol(
        symbol: String
    ) {

        onText(
            symbol
        )
    }

    Surface(
        modifier =
            modifier,

        color =
            Color.Black
    ) {

        if (
            symbolMode
        ) {

            AtlasSymbolKeyboard(
                onText = { symbol ->

                    sendSymbol(
                        symbol
                    )
                },

                onBackspace =
                    onBackspace,

                onEnter =
                    onEnter,

                onAlphabetMode = {

                    symbolMode =
                        false
                }
            )

        } else {

            AtlasAlphabetKeyboard(
                shiftActive =
                    shiftActive,

                onLetter = { character ->

                    sendLetter(
                        character
                    )
                },

                onText =
                    onText,

                onBackspace =
                    onBackspace,

                onEnter =
                    onEnter,

                onShift = {

                    shiftActive =
                        !shiftActive
                },

                onSymbolMode = {

                    shiftActive =
                        false

                    symbolMode =
                        true
                }
            )
        }
    }
}

@Composable
private fun AtlasAlphabetKeyboard(
    shiftActive: Boolean,
    onLetter: (Char) -> Unit,
    onText: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onShift: () -> Unit,
    onSymbolMode: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        3.dp,

                    vertical =
                        3.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {

        /*
         * NUMBER ROW
         */
        AtlasStringKeyRow(
            keys =
                listOf(
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "6",
                    "7",
                    "8",
                    "9",
                    "0"
                ),

            onKey = { value ->

                onText(
                    value
                )
            }
        )

        /*
         * QWERTY ROW
         */
        AtlasLetterRow(
            letters =
                "qwertyuiop",

            shiftActive =
                shiftActive,

            onLetter =
                onLetter
        )

        /*
         * HOME ROW
         */
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            12.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    3.dp
                )
        ) {

            "asdfghjkl"
                .forEach { character ->

                    AtlasTerminalKeyboardKey(
                        modifier =
                            Modifier
                                .weight(
                                    1f
                                ),

                        label =
                            if (
                                shiftActive
                            ) {

                                character
                                    .uppercaseChar()
                                    .toString()

                            } else {

                                character
                                    .toString()
                            },

                        onClick = {

                            onLetter(
                                character
                            )
                        }
                    )
                }
        }

        /*
         * SHIFT / BOTTOM LETTER ROW
         */
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    3.dp
                )
        ) {

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            1.5f
                        ),

                label =
                    if (
                        shiftActive
                    ) {

                        "SHIFT*"

                    } else {

                        "SHIFT"
                    },

                highlighted =
                    shiftActive,

                onClick =
                    onShift
            )

            "zxcvbnm"
                .forEach { character ->

                    AtlasTerminalKeyboardKey(
                        modifier =
                            Modifier
                                .weight(
                                    1f
                                ),

                        label =
                            if (
                                shiftActive
                            ) {

                                character
                                    .uppercaseChar()
                                    .toString()

                            } else {

                                character
                                    .toString()
                            },

                        onClick = {

                            onLetter(
                                character
                            )
                        }
                    )
                }

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            1.5f
                        ),

                label =
                    "BKSP",

                onClick =
                    onBackspace
            )
        }

        /*
         * TERMINAL CONTROL ROW
         */
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    3.dp
                )
        ) {

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            1.2f
                        ),

                label =
                    "SYM",

                onClick =
                    onSymbolMode
            )

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            4f
                        ),

                label =
                    "SPACE",

                onClick = {

                    onText(
                        " "
                    )
                }
            )

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            1.4f
                        ),

                label =
                    "ENT",

                onClick =
                    onEnter
            )
        }
    }
}

@Composable
private fun AtlasSymbolKeyboard(
    onText: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onAlphabetMode: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        3.dp,

                    vertical =
                        3.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {

        /*
         * COMMON SHELL SYMBOLS
         */
        AtlasStringKeyRow(
            keys =
                listOf(
                    "!",
                    "@",
                    "#",
                    "$",
                    "%",
                    "^",
                    "&",
                    "*",
                    "(",
                    ")"
                ),

            onKey =
                onText
        )

        /*
         * OPERATORS / BRACKETS
         */
        AtlasStringKeyRow(
            keys =
                listOf(
                    "-",
                    "_",
                    "=",
                    "+",
                    "[",
                    "]",
                    "{",
                    "}"
                ),

            onKey =
                onText
        )

        /*
         * PATH / SHELL SYMBOLS
         */
        AtlasStringKeyRow(
            keys =
                listOf(
                    "/",
                    "\\",
                    "|",
                    "~",
                    "`",
                    "'",
                    "\""
                ),

            onKey =
                onText
        )

        /*
         * PUNCTUATION
         */
        AtlasStringKeyRow(
            keys =
                listOf(
                    ";",
                    ":",
                    ",",
                    ".",
                    "<",
                    ">",
                    "?"
                ),

            onKey =
                onText
        )

        /*
         * RETURN TO LETTERS / EDITING
         */
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    3.dp
                )
        ) {

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            1.3f
                        ),

                label =
                    "ABC",

                onClick =
                    onAlphabetMode
            )

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            3.5f
                        ),

                label =
                    "SPACE",

                onClick = {

                    onText(
                        " "
                    )
                }
            )

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            1.4f
                        ),

                label =
                    "BKSP",

                onClick =
                    onBackspace
            )

            AtlasTerminalKeyboardKey(
                modifier =
                    Modifier
                        .weight(
                            1.2f
                        ),

                label =
                    "ENT",

                onClick =
                    onEnter
            )
        }
    }
}

@Composable
private fun AtlasLetterRow(
    letters: String,
    shiftActive: Boolean,
    onLetter: (Char) -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {

        letters
            .forEach { character ->

                AtlasTerminalKeyboardKey(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        if (
                            shiftActive
                        ) {

                            character
                                .uppercaseChar()
                                .toString()

                        } else {

                            character
                                .toString()
                        },

                    onClick = {

                        onLetter(
                            character
                        )
                    }
                )
            }
    }
}

@Composable
private fun AtlasStringKeyRow(
    keys: List<String>,
    onKey: (String) -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {

        keys
            .forEach { key ->

                AtlasTerminalKeyboardKey(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        key,

                    onClick = {

                        onKey(
                            key
                        )
                    }
                )
            }
    }
}

@Composable
private fun AtlasTerminalKeyboardKey(
    modifier: Modifier = Modifier,
    label: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {

    OutlinedButton(
        modifier =
            modifier
                .height(
                    38.dp
                ),

        onClick =
            onClick,

        shape =
            RoundedCornerShape(
                6.dp
            ),

        border =
            BorderStroke(
                width =
                    if (
                        highlighted
                    ) {

                        2.dp

                    } else {

                        1.dp
                    },

                color =
                    AtlasKeyboardGreen
            ),

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
                AtlasKeyboardGreen,

            fontFamily =
                FontFamily.Monospace,

            fontSize =
                11.sp,

            maxLines =
                1
        )
    }
}