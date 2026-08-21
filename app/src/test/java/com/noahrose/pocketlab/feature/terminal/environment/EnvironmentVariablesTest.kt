package com.noahrose.pocketlab.feature.terminal.environment

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvironmentVariablesTest {

    @After
    fun cleanUp() {

        EnvironmentVariables.clearUserVariables()
    }

    @Test
    fun valueOf_returnsBuiltInUser() {

        assertEquals(
            "atlas",
            EnvironmentVariables.valueOf(
                "USER"
            )
        )
    }

    @Test
    fun valueOf_isCaseInsensitive() {

        assertEquals(
            "atlas",
            EnvironmentVariables.valueOf(
                "user"
            )
        )
    }

    @Test
    fun set_createsUserVariable() {

        assertTrue(
            EnvironmentVariables.set(
                name = "PROJECT",
                value = "Atlas Cyberdeck"
            )
        )

        assertEquals(
            "Atlas Cyberdeck",
            EnvironmentVariables.valueOf(
                "PROJECT"
            )
        )
    }

    @Test
    fun set_normalizesVariableName() {

        EnvironmentVariables.set(
            name = "project",
            value = "Atlas"
        )

        assertEquals(
            "Atlas",
            EnvironmentVariables.valueOf(
                "PROJECT"
            )
        )
    }

    @Test
    fun set_allowsUnderscoreNames() {

        assertTrue(
            EnvironmentVariables.set(
                name = "ATLAS_PROJECT",
                value = "Cyberdeck"
            )
        )
    }

    @Test
    fun set_rejectsNameStartingWithNumber() {

        assertFalse(
            EnvironmentVariables.set(
                name = "51PROJECT",
                value = "Area 51"
            )
        )
    }

    @Test
    fun set_rejectsInvalidCharacters() {

        assertFalse(
            EnvironmentVariables.set(
                name = "PROJECT-NAME",
                value = "Atlas"
            )
        )
    }

    @Test
    fun remove_deletesUserVariable() {

        EnvironmentVariables.set(
            name = "PROJECT",
            value = "Atlas"
        )

        assertTrue(
            EnvironmentVariables.remove(
                "PROJECT"
            )
        )

        assertNull(
            EnvironmentVariables.valueOf(
                "PROJECT"
            )
        )
    }

    @Test
    fun userVariable_canOverrideBuiltInVariable() {

        EnvironmentVariables.set(
            name = "HOSTNAME",
            value = "atlas-lab"
        )

        assertEquals(
            "atlas-lab",
            EnvironmentVariables.valueOf(
                "HOSTNAME"
            )
        )
    }
}