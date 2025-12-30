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
import it.unibo.alchemist.boundary.dsl.model.AvailableIncarnations as Inc
import it.unibo.alchemist.boundary.dsl.model.SimulationContext
import it.unibo.alchemist.boundary.dsl.model.SimulationContextImpl
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.jvm.optionals.getOrElse
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
        builder: SimulationContextImpl<T, P>,
        envBuilder: () -> Environment<T, P>,
    ): Loader = object : DSLLoader<T, P>(builder, envBuilder) {
        override val constants: Map<String, Any?> = emptyMap()
        override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
        override val variables: Map<String, Variable<*>> = builder.variablesContext.variables
        override val remoteDependencies: List<String> = emptyList()
        override val launcher: Launcher = builder.launcher
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
        environment: () -> Environment<T, P>,
        block: SimulationContext<T, P>.() -> Unit,
    ): Loader {
        val ctx = SimulationContextImpl(incarnation)
        @Suppress("UNCHECKED_CAST")
        ctx.apply(block)
        return createLoader(ctx, environment)
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
            RandomGenerator,
            Environment<T, Euclidean2DPosition>
        ) SimulationContext<T, Euclidean2DPosition>.() -> Unit,
    ): Loader {
        @Suppress("UNCHECKED_CAST")
        val defaultEnv = { Continuous2DEnvironment(incarnation) }
        val ctx = SimulationContextImpl(incarnation)
        @Suppress("UNCHECKED_CAST")
        ctx.apply {
            context(ctx.simulationGenerator, ctx.environment) {
                block()
            }
        }
        return createLoader(ctx, defaultEnv)
    }
}
