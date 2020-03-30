import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.Alchemist
import org.apache.commons.io.output.TeeOutputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.security.Permission

/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

data class CatchableExit(val status: Int) : SecurityException()
data class ProcessResult(val status: Int, val output: String)

fun runCatchingExit(test: () -> Unit): Throwable? {
    val manager = System.getSecurityManager()
    val securityManager: SecurityManager = object : SecurityManager() {
        override fun checkPermission(permission: Permission) {
            if (permission.name.startsWith("exitVM")) {
                val exitStatus = Regex("""exitVM\.(\d+)""")
                    .matchEntire(permission.name)
                    ?.destructured
                    ?.component1()
                    ?.toInt()
                throw exitStatus?.let { CatchableExit(it) }
                    ?: IllegalStateException("Unparseable vm exit permission: ${permission.name}")
            }
        }
    }
    System.setSecurityManager(securityManager)
    return runCatching(test).exceptionOrNull()
        .also { System.setSecurityManager(manager) }
}

fun runWithOptions(vararg commands: String, test: ProcessResult.() -> Unit) {
    val sysout = System.out
    val bytes = ByteArrayOutputStream()
    val tee = TeeOutputStream(System.out, bytes)
    System.setOut(PrintStream(tee))
    val exit = runCatchingExit {
        Alchemist.main(commands.toList().toTypedArray())
    }
    System.setOut(sysout)
    when {
        exit is CatchableExit -> test(ProcessResult(exit.status, bytes.toString()))
        exit is Throwable -> throw exit
        else -> throw IllegalStateException("Execution did not end with an exit code.\n$bytes")
    }
}

class TestCLI : StringSpec({
    "help should get printed with --help" {
//        val sysout = System.out
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
})
