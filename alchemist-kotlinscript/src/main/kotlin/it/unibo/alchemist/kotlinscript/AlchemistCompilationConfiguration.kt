/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.kotlinscript

import it.unibo.alchemist.boundary.kotlindsl.SimulationContext
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.maps.environments.OSMEnvironment
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

/**
 * Compilation configuration for Alchemist scripts.
 */
object AlchemistCompilationConfiguration : ScriptCompilationConfiguration({
    val classes = listOf(
        ProtelisIncarnation::class,
        OSMEnvironment::class,
    )
    val importsFromClasses = classes
        .mapNotNull { it.qualifiedName?.substringBeforeLast('.') }
        .distinct()
        .map { "$it.*" }
    defaultImports(
        *importsFromClasses.toTypedArray(),
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
        "it.unibo.alchemist.model.positionfilters.*",
        "it.unibo.alchemist.model.properties.*",
        "it.unibo.alchemist.model.routes.*",
        "it.unibo.alchemist.model.reactions.*",
        "it.unibo.alchemist.model.terminators.*",
        "it.unibo.alchemist.model.timedistributions.*",
        "it.unibo.alchemist.model.times.*",
        "it.unibo.alchemist.boundary.properties.*",
        "it.unibo.alchemist.boundary.exporters.*",
        "it.unibo.alchemist.boundary.extractors.*",
        "it.unibo.alchemist.boundary.kotlindsl.*",
        "it.unibo.alchemist.boundary.launchers.*",
        "it.unibo.alchemist.boundary.statistic.*",
        "it.unibo.alchemist.boundary.exportfilters.*",
        "it.unibo.alchemist.boundary.variables.*",
    )
    hostConfiguration
    compilerOptions.append("-Xcontext-parameters")
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
    jvm {
        dependenciesFromClassContext(AlchemistScript::class, wholeClasspath = true)
        dependenciesFromClassContext(ProtelisIncarnation::class, wholeClasspath = true)
        dependenciesFromClassContext(SimulationContext::class, wholeClasspath = true)
    }
}) {
    // See: https://docs.oracle.com/javase/8/docs/platform/serialization/spec/input.html
    @Suppress("UnusedPrivateFunction")
    private fun readResolve(): Any = AlchemistCompilationConfiguration
}
