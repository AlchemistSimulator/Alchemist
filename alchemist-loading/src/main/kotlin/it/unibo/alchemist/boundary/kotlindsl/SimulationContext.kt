/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty

/**
 * Top-level DSL scope for defining a simulation scenario.
 *
 * A [SimulationContext] is responsible for collecting all scenario components that are needed to build a runnable
 * simulation, including:
 * - the [Environment] and its configuration (deployments, layers, global programs, network model, terminators);
 * - exporters and output monitors;
 * - the [Launcher] used to run the simulation;
 * - scenario variables, exposed as delegated read-only properties.
 *
 * Implementations typically distinguish between two random generators:
 * - a *scenario* generator, used while constructing the scenario (e.g., during deployments or initialization);
 * - a *simulation* generator, used for components that require randomness tied to simulation execution.
 *
 * @param T the concentration type used by the simulation.
 * @param P the position type used by the environment.
 */
interface SimulationContext<T, P : Position<P>> {

    /**
     * Sets the simulation [environment] and configures it through [block].
     *
     * The [block] is executed with the provided environment as a context receiver, and with an [EnvironmentContext]
     * as receiver, enabling environment-level configuration (deployments, layers, global programs, linking rules,
     * terminators).
     *
     * @param environment the environment instance to use for the simulation.
     * @param block the environment configuration block.
     */
    fun <E : Environment<T, P>> environment(environment: E, block: context(E) EnvironmentContext<T, P>.() -> Unit)

    /**
     * Registers an [Exporter] and configures which [it.unibo.alchemist.boundary.Extractor] instances it should use.
     *
     * The [block] is executed with an [ExporterContext] receiver, typically allowing extractors to be collected via
     * unary-minus notation and then bound to the exporter.
     *
     * @param exporter the exporter to register.
     * @param block the exporter configuration block.
     */
    fun exportWith(exporter: Exporter<T, P>, block: ExporterContext<T, P>.() -> Unit)

    /**
     * Sets the random generator used during scenario construction.
     *
     * The scenario random generator is meant for operations that happen while building the scenario configuration,
     * such as deployments and initialization steps performed before the simulation starts.
     *
     * @param randomGenerator the random generator to use for scenario construction.
     */
    fun scenarioRandomGenerator(randomGenerator: RandomGenerator)

    /**
     * Sets the scenario random generator to a [MersenneTwister] initialized with [seed].
     *
     * This is a convenience method equivalent to calling [scenarioRandomGenerator] with a new [MersenneTwister].
     *
     * @param seed the seed to initialize the generator.
     */
    fun scenarioSeed(seed: Long) = scenarioRandomGenerator(MersenneTwister(seed))

    /**
     * Sets the random generator used for simulation-related components.
     *
     * This generator is intended for components created as part of the scenario that require randomness associated
     * with simulation execution (e.g., reactions/time distributions or other simulation-time behaviors),
     * depending on the implementation.
     *
     * @param randomGenerator the random generator to use for simulation-related components.
     */
    fun simulationRandomGenerator(randomGenerator: RandomGenerator)

    /**
     * Sets the simulation random generator to a [MersenneTwister] initialized with [seed].
     *
     * This is a convenience method equivalent to calling [simulationRandomGenerator] with a new [MersenneTwister].
     *
     * @param seed the seed to initialize the generator.
     */
    fun simulationSeed(seed: Long) = simulationRandomGenerator(MersenneTwister(seed))

    /**
     * Registers an [OutputMonitor] to observe the simulation execution.
     *
     * @param monitor the monitor to add.
     */
    fun monitor(monitor: OutputMonitor<T, P>)

    /**
     * Sets the [Launcher] used to run the simulation.
     *
     * @param launcher the launcher to use.
     */
    fun launcher(launcher: Launcher)

    /**
     * Declares a scenario [Variable] and returns a read-only property delegate that provides its value.
     *
     * Implementations typically associate the variable with the name of the delegated property and resolve its value
     * from the runtime-provided variable map, falling back to [Variable.default] when no override is provided.
     *
     * @param variable the variable definition.
     * @return a property delegate providing the resolved variable value.
     */
    fun <V : Serializable> variable(variable: Variable<out V>): ReadOnlyProperty<Any?, V>
}
