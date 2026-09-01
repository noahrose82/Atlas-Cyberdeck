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

    fun sendCharacter(
        character: Char
    ) {

        val outputCharacter =
            if (
                shiftActive &&
                character.isLetter()
            ) {

                character.uppercaseChar()

            } else {

                character
            }

        onText(
            outputCharacter.toString()
        )

        if (
            shiftActive
        ) {

            shiftActive =
                false
        }
    }

    Surface(
        modifier =
            modifier,

        color =
            Color.Black
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
                    3.dp
                )
        ) {

            /*
             * NUMBER ROW
             */
            AtlasKeyboardCharacterRow(
                characters =
                    "1234567890",

                shiftActive =
                    false,

                onCharacter = { character ->

                    sendCharacter(
                        character
                    )
                }
            )

            /*
             * QWERTY ROW
             */
            AtlasKeyboardCharacterRow(
                characters =
                    "qwertyuiop",

                shiftActive =
                    shiftActive,

                onCharacter = { character ->

                    sendCharacter(
                        character
                    )
                }
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
                                10.dp
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

                                sendCharacter(
                                    character
                                )
                            }
                        )
                    }
            }

            /*
             * SHIFT + BOTTOM LETTER ROW
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
                                1.4f
                            ),

                    label =
                        if (
                            shiftActive
                        ) {

                            "SHIFT*"

                        } else {

                            "SHIFT"
                        },

                    onClick = {

                        shiftActive =
                            !shiftActive
                    }
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

                                sendCharacter(
                                    character
                                )
                            }
                        )
                    }

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
            }

            /*
             * SPACE / ENTER ROW
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
                                4f
                            ),

                    label =
                        "SPACE",

                    onClick = {

                        onText(
                            " "
                        )

                        shiftActive =
                            false
                    }
                )

                AtlasTerminalKeyboardKey(
                    modifier =
                        Modifier
                            .weight(
                                1.5f
                            ),

                    label =
                        "ENT",

                    onClick = {

                        onEnter()

                        shiftActive =
                            false
                    }
                )
            }
        }
    }
}

@Composable
private fun AtlasKeyboardCharacterRow(
    characters: String,
    shiftActive: Boolean,
    onCharacter: (Char) -> Unit
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

        characters
            .forEach { character ->

                AtlasTerminalKeyboardKey(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            ),

                    label =
                        if (
                            shiftActive &&
                            character.isLetter()
                        ) {

                            character
                                .uppercaseChar()
                                .toString()

                        } else {

                            character
                                .toString()
                        },

                    onClick = {

                        onCharacter(
                            character
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
                10.dp
            ),

        border =
            BorderStroke(
                width =
                    1.dp,

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