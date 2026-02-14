/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
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
    defaultImports(
        "it.unibo.alchemist.boundary.kotlindsl.*",
        "it.unibo.alchemist.boundary.dsl.Dsl.incarnation",
        "it.unibo.alchemist.boundary.dsl.model.AvailableIncarnations.*",
        "it.unibo.alchemist.boundary.dsl.model.Incarnation.*",
        "it.unibo.alchemist.boundary.dsl.generated.*",
        "it.unibo.alchemist.boundary.dsl.*",
        "it.unibo.alchemist.boundary.dsl.Dsl.*",
        "it.unibo.alchemist.model.*",
        "it.unibo.alchemist.model.deployments.*",
        "it.unibo.alchemist.model.incarnations.*",
        "it.unibo.alchemist.model.actions.*",
        "it.unibo.alchemist.model.conditions.*",
        "it.unibo.alchemist.model.environments.*",
        "it.unibo.alchemist.model.geometry.*",
        "it.unibo.alchemist.model.layers.*",
        "it.unibo.alchemist.model.linkingrules.*",
        "it.unibo.alchemist.model.maps.actions.*",
        "it.unibo.alchemist.model.maps.deployments.*",
        "it.unibo.alchemist.model.maps.environments.*",
        "it.unibo.alchemist.model.movestrategies.*",
        "it.unibo.alchemist.model.neighborhoods.*",
        "it.unibo.alchemist.model.nodes.*",
        "it.unibo.alchemist.model.positions.*",
        "it.unibo.alchemist.model.positionfilters.And",
        "it.unibo.alchemist.model.positionfilters.Or",
        "it.unibo.alchemist.model.positionfilters.Not",
        "it.unibo.alchemist.model.positionfilters.Xor",
        "it.unibo.alchemist.model.properties.*",
        "it.unibo.alchemist.model.routes.*",
        "it.unibo.alchemist.model.reactions.*",
        "it.unibo.alchemist.model.terminators.*",
        "it.unibo.alchemist.model.timedistributions.*",
        "it.unibo.alchemist.boundary.properties.*",
        "it.unibo.alchemist.boundary.dsl.aliases.*",
        "it.unibo.alchemist.boundary.exporters.*",
        "it.unibo.alchemist.boundary.extractors.*",
        "it.unibo.alchemist.boundary.launchers.*",
        "it.unibo.alchemist.boundary.statistic.*",
        "it.unibo.alchemist.boundary.exportfilters.*",
        "it.unibo.alchemist.boundary.variables.*",
        "it.unibo.alchemist.boundary.dsl.util.LoadingSystemLogger.logger",
    )
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
    jvm {
        dependenciesFromClassContext(AlchemistScript::class, wholeClasspath = true)
        compilerOptions.append("-Xcontext-parameters")
    }
}) {
    /**
     * Return the singleton instance on deserialization.
     * This is intentionally private and used by Java serialization. Detekt flags it as unused,
     * but it is required by the serialization mechanism.
     *
     * See: https://docs.oracle.com/javase/8/docs/platform/serialization/spec/input.html
     */
    @Suppress("unused")
    private fun readResolve(): Any = AlchemistCompilationConfiguration
}
