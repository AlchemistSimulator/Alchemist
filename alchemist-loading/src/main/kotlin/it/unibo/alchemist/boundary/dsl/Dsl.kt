/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.dsl.model.Incarnation as Inc
import it.unibo.alchemist.boundary.dsl.model.SimulationContext
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.jvm.optionals.getOrElse

object Dsl {
    fun <T, P : Position<P>> createLoader(dsl: SimulationContext<T, P>): Loader = object : DslLoader(dsl) {
        override val constants: Map<String, Any?> = emptyMap() // not needed
        override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap() // not needed
        override val variables: Map<String, Variable<*>> = dsl.variablesContext.variables
        override val remoteDependencies: List<String> = emptyList() // not needed
        override val launcher: Launcher = dsl.launcher
    }

    fun <T, P : Position<P>> Inc.incarnation(): Incarnation<T, P> =
        SupportedIncarnations.get<T, P>(this.name).getOrElse {
            throw IllegalArgumentException("Incarnation $this not supported")
        }

    fun <T, P : Position<P>> simulation(
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        block: SimulationContext<T, P>.() -> Unit,
    ): Loader = createLoader(SimulationContext(incarnation, environment).apply(block))

    fun <T, P : Position<P>> simulation(
        incarnation: Incarnation<T, P>,
        block: SimulationContext<T, Euclidean2DPosition>.() -> Unit,
    ): Loader {
        @Suppress("UNCHECKED_CAST")
        val defaultEnv = Continuous2DEnvironment(incarnation as Incarnation<T, Euclidean2DPosition>)
        val ctx = SimulationContext(incarnation, defaultEnv).apply(block)
        return createLoader(ctx)
    }
}
