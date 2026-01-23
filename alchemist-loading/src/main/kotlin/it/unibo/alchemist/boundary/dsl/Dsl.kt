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
import it.unibo.alchemist.boundary.dsl.model.SimulationContext
import it.unibo.alchemist.boundary.dsl.model.SimulationContextImpl
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.random.RandomGenerator

/**
 * Marker annotation for Alchemist DSL elements.
 *
 * This annotation is used to mark DSL context classes and functions,
 * preventing scope pollution in DSL blocks.
 */
@DslMarker
annotation class AlchemistDsl

/**
 * Main DSL object for creating Alchemist simulations.
 *
 * This object provides factory methods for creating simulation loaders
 * and configuring Alchemist simulations using a type-safe DSL.
 */
object Dsl {
    /**
     * Creates a loader from a simulation context.
     *
     * @param dsl The simulation context.
     * @return A loader instance.
     */
    fun <T, P : Position<P>> createLoader(
        builder: SimulationContext<T, P>,
        envBuilder: () -> Environment<T, P>,
    ): Loader = object : DSLLoader(builder) {
        override val constants: Map<String, Any?> = emptyMap()
        override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
        override val variables: Map<String, Variable<*>> = builder.variablesContext.variables
        override val remoteDependencies: List<String> = emptyList()
        override val launcher: Launcher = builder.launcher

        @Suppress("UNCHECKED_CAST")
        override fun <T, P : Position<P>> envFactory(): Environment<T, P> = envBuilder() as Environment<T, P>
    }

    /**
     * Creates a simulation with a custom environment.
     *
     * @param incarnation The incarnation instance.
     * @param environmentFactory The environment instance.
     * @param block The simulation configuration block.
     * @return A loader instance.
     */
    fun <T, P : Position<P>> simulation(
        incarnation: Incarnation<T, P>,
        environmentFactory: context(Incarnation<T, P>) () -> Environment<T, P>,
        block: context(
            Incarnation<T, P>,
            RandomGenerator,
            Environment<T, P>,
        ) SimulationContext<T, P>.() -> Unit,
    ): Loader {
        val ctx = SimulationContextImpl(incarnation, environmentFactory)
        ctx.apply {
            context(incarnation, ctx.simulationGenerator, ctx.environment) {
                block()
            }
        }
        return createLoader(ctx) {
            context(incarnation) {
                environmentFactory()
            }
        }
    }

    /**
     * Creates a simulation with a default 2D continuous environment.
     *
     * @param incarnation The incarnation instance.
     * @param block The simulation configuration block.
     * @return A loader instance.
     */
    fun <T> simulation(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        block: context(
            Incarnation<T, Euclidean2DPosition>,
            RandomGenerator,
            Environment<T, Euclidean2DPosition>,
        ) SimulationContext<T, Euclidean2DPosition>.() -> Unit,
    ): Loader = simulation(incarnation, { Continuous2DEnvironment(incarnation) }, block)
}
