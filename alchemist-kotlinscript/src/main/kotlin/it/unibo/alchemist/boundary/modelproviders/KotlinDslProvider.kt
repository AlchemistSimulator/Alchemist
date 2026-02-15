/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelproviders

import it.unibo.alchemist.boundary.AlchemistLoaderProvider
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.kotlinscript.AlchemistScript
import java.io.InputStream
import java.io.Reader
import java.net.URL
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

/**
 * Provider for loading Alchemist simulations from Kotlin DSL scripts.
 */
object KotlinDslProvider : AlchemistLoaderProvider {
    override val fileExtensions: Regex = "(?i)kts".toRegex()

    private val host = BasicJvmScriptingHost()
    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<AlchemistScript>()
    private val baseClassLoader = this::class.java.classLoader

    /**
     * Evaluation configuration for script execution.
     */
    private val evaluationConfiguration = ScriptEvaluationConfiguration {
        jvm {
            baseClassLoader(this@KotlinDslProvider.baseClassLoader)
        }
    }
    override fun from(input: String): Loader {
        val result = host.eval(input.toScriptSource(), compilationConfiguration, evaluationConfiguration)
        val errors = result.reports.filter { it.severity == ScriptDiagnostic.Severity.ERROR }
        require(errors.isEmpty()) { errors.joinToString("\n") { it.message } }
        return when (val resultValue = result.valueOrThrow().returnValue) {
            is ResultValue.Value -> {
                val value = resultValue.value
                check(value is Loader) {
                    "Script must return a Loader; got ${value?.let { "$it: ${it::class.qualifiedName}" }}"
                }
                value
            }
            is ResultValue.Error -> {
                throw resultValue.error
            }
            is ResultValue.Unit ->
                error("Script returned Unit; expected a Loader (use 'simulation { ... }' as the last statement)")
            is ResultValue.NotEvaluated -> {
                error("Script was not evaluated")
            }
        }
    }

    override fun from(input: Reader): Loader = from(input.readText())

    override fun from(input: InputStream): Loader = from(input.reader())

    override fun from(input: URL): Loader = from(input.openStream())
}
