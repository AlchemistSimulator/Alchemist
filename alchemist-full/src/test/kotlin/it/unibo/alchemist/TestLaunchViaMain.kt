/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import org.junit.jupiter.api.Test
import org.slf4j.event.Level
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests the execution of the Alchemist main. It triggers a VM exit and must get launched in its own JVM.
 */
class TestLaunchViaMain {
    @Test
    fun `A simple simulation should be executable in headless mode`() {
        Alchemist.main(arrayOf("run", "simulation.yml"))
    }

    @Test
    fun `The default logging level should be warn`() {
        option(null).shouldLogAtLevel(Level.WARN)
    }

    @Test
    fun `Verbosity levels should be controllable via command line options`() {
        assertTrue(launchWithLoggingOption("off").isEmpty(), "Expected empty output for verbosity level 'off'")
        option("error").shouldLogAtLevel(Level.ERROR)
        option("info").shouldLogAtLevel(Level.INFO)
        option("debug").shouldLogAtLevel(Level.DEBUG)
        option("all").shouldLogAtLevel(Level.TRACE)
    }

    companion object {
        private fun launchWithLoggingOption(option: String?): String {
            val writer = ByteArrayOutputStream()
            val systemOut = System.out
            System.setOut(PrintStream(writer, true, UTF_8))
            var options = arrayOf("run", "logging.yml")
            if (option != null) {
                options += arrayOf("--verbosity", option)
            }
            Alchemist.main(options)
            System.setOut(systemOut)
            return writer.toString(UTF_8)
        }

        private class LevelContext(
            val option: String? = null,
        ) {
            fun shouldLogAtLevel(level: Level) {
                val output = launchWithLoggingOption(option)
                Level.entries.forEach {
                    if (it.ordinal <= level.ordinal) {
                        assertTrue(it.name in output, "Expected output to contain '${it.name}' at level $level")
                    } else {
                        assertFalse(it.name in output, "Expected output to NOT contain '${it.name}' above level $level")
                    }
                }
            }
        }

        private fun option(option: String? = null): LevelContext = LevelContext(option)
    }
}
