/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.dsl.DslLoader
import it.unibo.alchemist.boundary.dsl.model.Incarnation as Inc
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.jvm.optionals.getOrElse

class SimulationContext {
    var incarnation: Inc = Inc.SAPERE
    var envCtx: EnvironmentContext<*, *>? = null

    fun getDefault(): Environment<Any, Euclidean2DPosition> = Continuous2DEnvironment(this.getIncarnation())
    fun <T, P : Position<P>> environment(env: Environment<T, P>, block: EnvironmentContext<T, P>.() -> Unit) {
        envCtx = EnvironmentContext(this, env).apply(block)
    }
    internal fun <T, P : Position<P>> getIncarnation(): Incarnation<T, P> =
        SupportedIncarnations.get<T, P>(incarnation.name).getOrElse {
            throw IllegalArgumentException("Incarnation $incarnation not supported")
        }
}

fun createLoader(simBuilder: SimulationContext): Loader = object : DslLoader(simBuilder) {
    override val constants: Map<String, Any?> = emptyMap()
    override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
    override val variables: Map<String, Variable<*>> = emptyMap()
    override val remoteDependencies: List<String> = emptyList()
    override val launcher: Launcher = DefaultLauncher()
}

fun simulation(block: SimulationContext.() -> Unit): Loader {
    val sim = SimulationContext().apply(block)
    return createLoader(sim)
}
