package com.noahrose.pocketlab.feature.terminal.chaining

data class ConditionalCommand(
    val command: String,
    val operatorBefore: ConditionalOperator?
)