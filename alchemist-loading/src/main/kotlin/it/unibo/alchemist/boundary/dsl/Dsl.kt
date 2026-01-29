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
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
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
    private fun <T, P : Position<P>, E : Environment<T, P>> createLoader(builder: SimulationContext<T, P, E>): Loader =
        object : DSLLoader(builder) {
            override val constants: Map<String, Any?> = emptyMap()
            override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
            override val variables: Map<String, Variable<*>> = builder.variablesContext.variables
            override val remoteDependencies: List<String> = emptyList()
            override val launcher: Launcher = builder.launcher
        }

    /**
     * Creates a simulation with a custom environment.
     *
     * @param incarnation The incarnation instance.
     * @param block The simulation configuration block.
     * @return A loader instance.
     */
    fun <T, P : Position<P>, E : Environment<T, P>> simulation(
        incarnation: Incarnation<T, P>,
        block: context(
            Incarnation<T, P>,
            RandomGenerator,
        ) SimulationContext<T, P, E>.() -> Unit,
    ): Loader {
        val ctx = SimulationContextImpl<T, P, E>(incarnation)
        ctx.apply {
            context(incarnation, ctx.scenarioGenerator) {
                block()
            }
        }
        return createLoader(ctx)
    }

    /**
     * Creates a 2D simulation with a Euclidean2DEnvironment.
     */
    fun <T> simulation2D(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        block: context(
            Incarnation<T, Euclidean2DPosition>,
            RandomGenerator,
        ) SimulationContext<T, Euclidean2DPosition, Euclidean2DEnvironment<T>>.() -> Unit,
    ): Loader = simulation<T, Euclidean2DPosition, Euclidean2DEnvironment<T>>(incarnation, block)
}
