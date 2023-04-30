/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.beEmpty
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.slf4j.event.Level
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Tests the execution of the Alchemist main. It triggers a VM exit and must get launched in its own JVM.
 */
class TestLaunchViaMain : StringSpec({
    "A simple simulation should be executable in headless mode" {
        Alchemist.main(arrayOf("-y", "simulation.yml", "-hl", "-t", "2"))
    }

    "The default logging level should be warn" {
        option(null) shouldLogAtLevel Level.WARN
    }

    "Verbosity levels should be controllable via command line options" {
        launchWithLoggingOption("-qq") should beEmpty()
        option("-q") shouldLogAtLevel Level.ERROR
        option("-v") shouldLogAtLevel Level.INFO
        option("-vv") shouldLogAtLevel Level.DEBUG
        option("-vvv") shouldLogAtLevel Level.TRACE
    }
}) {
    companion object {

        private fun launchWithLoggingOption(option: String?): String {
            val writer = ByteArrayOutputStream()
            val systemOut = System.out
            System.setOut(PrintStream(writer, true, UTF_8))
            val loggerOptions = if (option == null) arrayOf() else arrayOf(option)
            Alchemist.main(arrayOf("-y", "logging.yml", "-hl") + loggerOptions)
            System.setOut(systemOut)
            return writer.toString(UTF_8)
        }

        private class LevelContext(val option: String? = null) {
            infix fun shouldLogAtLevel(level: Level) {
                val output = launchWithLoggingOption(option)
                Level.values().forEach {
                    if (it.ordinal <= level.ordinal) {
                        output shouldContain it.name
                    } else {
                        output shouldNotContain it.name
                    }
                }
            }
        }

        private fun option(option: String? = null): LevelContext = LevelContext(option)
    }
}
