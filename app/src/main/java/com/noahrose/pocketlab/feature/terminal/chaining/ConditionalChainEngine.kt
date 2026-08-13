package com.noahrose.pocketlab.feature.terminal.chaining

import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus

object ConditionalChainEngine {

    fun execute(
        command: String,
        executor: (String) -> Unit
    ): Boolean {

        val chain =
            ConditionalChainParser.parse(command)
                ?: return false

        var previousSucceeded = true

        chain.forEachIndexed { index, item ->

            val shouldExecute =
                if (index == 0) {

                    true

                } else {

                    when (item.operatorBefore) {

                        ConditionalOperator.AND ->
                            previousSucceeded

                        ConditionalOperator.OR ->
                            !previousSucceeded

                        null ->
                            true
                    }
                }

            if (shouldExecute) {

                executor(
                    item.command
                )

                previousSucceeded =
                    ExecutionStatus.wasSuccessful()
            }
        }

        return true
    }
}