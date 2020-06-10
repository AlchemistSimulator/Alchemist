import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import it.unibo.alchemist.Alchemist
import org.apache.commons.io.output.TeeOutputStream
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

data class ProcessResult(val status: Int, val output: String)

fun runWithOptions(vararg commands: String, test: ProcessResult.() -> Unit) {
    val sysout = System.out
    val bytes = ByteArrayOutputStream()
    val tee = TeeOutputStream(System.out, bytes)
    System.setOut(PrintStream(tee))
    Alchemist.enableTestMode()
    val exit = runCatching {
        Alchemist.main(commands.toList().toTypedArray())
    }.exceptionOrNull()
    val exitStatus = when {
        exit == null -> 0
        exit is Alchemist.AlchemistWouldHaveExitedException -> exit.exitStatus
        else -> throw IllegalStateException(exit)
    }
    System.setOut(sysout)
    test(ProcessResult(exitStatus, bytes.toString()))
}

class TestCLI : StringSpec({
    "help should get printed with --help" {
        runWithOptions("--help") {
            status shouldBe 0
            output shouldContain "batch"
            output shouldContain "help"
            output shouldContain "effect"
        }
    }
    "execution of a file should launch" {
        runWithOptions("-y", "simplesimulation.yml") {
            status shouldBe 0
        }
    }
    "execution of a batch should work" {
        runWithOptions("-y", "simplesimulation.yml", "-b", "-var", "fiz,baz") {
            status shouldBe 0
        }
    }
    "logger should not be nop" {
        LoggerFactory.getLogger(Alchemist::class.java)::class.java.name shouldNotContain "NOP"
    }
})
