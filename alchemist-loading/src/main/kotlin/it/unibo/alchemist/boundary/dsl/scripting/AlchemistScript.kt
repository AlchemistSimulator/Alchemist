/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.scripting

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

/**
 * Base interface for Alchemist Kotlin DSL scripts.
 */
@KotlinScript(
    displayName = "Alchemist Kotlin DSL",
    fileExtension = "alchemist.kts",
    compilationConfiguration = AlchemistCompilationConfiguration::class,
)
interface AlchemistScript

/**
 * Compilation configuration for Alchemist scripts.
 */
object AlchemistCompilationConfiguration : ScriptCompilationConfiguration({
    defaultImports(*loadDefaultImports().toTypedArray())

    jvm {
        dependenciesFromClassContext(AlchemistScript::class, wholeClasspath = true)
        compilerOptions.append("-Xcontext-parameters")
    }
}) {
    @Suppress("UnusedPrivateMember")
    private fun readResolve(): Any = AlchemistCompilationConfiguration
}

private fun loadDefaultImports(): List<String> {
    val defaultImports = listOf(
        "it.unibo.alchemist.boundary.dsl.Dsl.simulation",
        "it.unibo.alchemist.boundary.dsl.model.Incarnation.*",
        "it.unibo.alchemist.boundary.dsl.generated.*",
        "it.unibo.alchemist.boundary.dsl.*",
        "it.unibo.alchemist.model.*",
    )

    val configFileName = "alchemist-default-imports.json"
    val type = object : TypeToken<List<String>>() {}.type
    val mergedImports = defaultImports.toMutableList()
    val seenImports = defaultImports.toMutableSet()

    fun loadAndMergeImports(json: String) {
        try {
            val loadedImports = Gson().fromJson<List<String>>(json, type) ?: return
            loadedImports.forEach { import ->
                if (seenImports.add(import)) {
                    mergedImports.add(import)
                }
            }
        } catch (e: Exception) {
        }
    }

    val externalFile = File(configFileName)
    if (externalFile.exists() && externalFile.isFile) {
        loadAndMergeImports(externalFile.readText())
    } else {
        val resourceStream = AlchemistScript::class.java.classLoader
            .getResourceAsStream(configFileName)
        if (resourceStream != null) {
            val json = resourceStream.bufferedReader().use { it.readText() }
            loadAndMergeImports(json)
        }
    }

    return mergedImports
}
