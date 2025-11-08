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

/**
 * Main DSL object for creating Alchemist simulations.
 */
object Dsl {
    /**
     * Creates a loader from a simulation context.
     *
     * @param dsl The simulation context.
     * @return A loader instance.
     */
    fun <T, P : Position<P>> createLoader(dsl: SimulationContext<T, P>): Loader = object : SingleUseDslLoader(dsl) {
        override val constants: Map<String, Any?> = emptyMap() // not needed
        override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap() // not needed
        override val variables: Map<String, Variable<*>> = dsl.variablesContext.variables
        override val remoteDependencies: List<String> = emptyList() // not needed
        override val launcher: Launcher = dsl.launcher
    }

    /**
     * Converts an Incarnation enum to an Incarnation instance.
     *
     * @return The incarnation instance.
     */
    fun <T, P : Position<P>> Inc.incarnation(): Incarnation<T, P> =
        SupportedIncarnations.get<T, P>(this.name).getOrElse {
            throw IllegalArgumentException("Incarnation $this not supported")
        }

    /**
     * Creates a simulation with a custom environment.
     *
     * @param incarnation The incarnation instance.
     * @param environment The environment instance.
     * @param block The simulation configuration block.
     * @return A loader instance.
     */
    fun <T, P : Position<P>> simulation(
        incarnation: Incarnation<T, P>,
        environment: Environment<T, P>,
        block: SimulationContext<T, P>.() -> Unit,
    ): Loader {
        val ctx = SimulationContext(incarnation, environment)
        @Suppress("UNCHECKED_CAST")
        context(ctx.environment as Environment<*, *>, ctx.incarnation as Incarnation<*, *>) {
            ctx.apply(block)
        }
        return createLoader(ctx)
    }

    /**
     * Creates a simulation with a default 2D continuous environment.
     *
     * @param incarnation The incarnation instance.
     * @param block The simulation configuration block.
     * @return A loader instance.
     */
    fun <T, P : Position<P>> simulation(
        incarnation: Incarnation<T, P>,
        block: SimulationContext<T, Euclidean2DPosition>.() -> Unit,
    ): Loader {
        @Suppress("UNCHECKED_CAST")
        val defaultEnv = Continuous2DEnvironment(incarnation as Incarnation<T, Euclidean2DPosition>)
        val ctx = SimulationContext(incarnation, defaultEnv)
        @Suppress("UNCHECKED_CAST")
        context(ctx.environment, ctx.incarnation) {
            ctx.apply(block)
        }
        return createLoader(ctx)
    }
}
