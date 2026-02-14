/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.kotlinscript

import it.unibo.alchemist.boundary.Loader
import java.io.File
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

fun main(vararg args: String) {
    require(args.size == 1) { "usage: <app> <file.alchemist.kts>" }

    val scriptFile = File(args[0])
    val result = BasicJvmScriptingHost().eval(
        scriptFile.toScriptSource(),
        AlchemistCompilationConfiguration,
        ScriptEvaluationConfiguration {
//            jvm { mainArguments(scriptArgs) }
        }
    )

    result.reports
        .filter { it.severity > ScriptDiagnostic.Severity.DEBUG }
        .forEach { d -> System.err.println("${d.severity}: ${d.message}") }

    val eval = result.valueOrNull()
    val returned = (eval?.returnValue as? ResultValue.Value)?.value
    check(returned is Loader) {
        "The script should return a Loader, but it returned ${returned?.javaClass ?: "nothing"}"
    }
    returned.launcher.launch(returned)
}
