package com.noahrose.pocketlab.feature.terminal.redirection

data class RedirectionRequest(
    val command: String,
    val target: String,
    val type: RedirectionType
)