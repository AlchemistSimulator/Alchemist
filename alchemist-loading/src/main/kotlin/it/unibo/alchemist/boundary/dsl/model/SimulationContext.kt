/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.dsl.AlchemistDsl
import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import java.io.Serializable
import org.apache.commons.math3.random.RandomGenerator

/**
 * Main context interface for building and configuring Alchemist simulations using the DSL.
 *
 * This interface provides a type-safe way to configure simulations programmatically.
 * It serves as the entry point for DSL users to define
 * all aspects of a simulation including deployments, programs, monitors, exporters, and more.
 *
 * ## Usage Example
 *
 * ```kotlin
 * simulation(incarnation, environment) {
 *     networkModel = ConnectWithinDistance(0.5)
 *     deployments {
 *         deploy(Grid(-5, -5, 5, 5, 0.25, 0.25)) {
 *             all {
 *                 molecule = "moleculeName"
 *                 concentration = 1.0
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of molecule concentration used in the simulation
 * @param P The type of position used in the environment, must extend [Position]
 *
 * @see [it.unibo.alchemist.boundary.dsl.Dsl] for creating simulation contexts
 * @see [DeploymentsContextImpl] for deployment configuration
 * @see [ProgramsContextImpl] for program configuration
 * @see [ExporterContextImpl] for exporter configuration
 * @see [LayerContextImpl] for layer configuration
 */
@AlchemistDsl
interface SimulationContext<T, P : Position<P>> {
    /**
     * The incarnation instance that defines how molecules, nodes, and reactions are created.
     *
     * ## Creating an Incarnation
     *
     * Incarnations are created from the [AvailableIncarnations] enum using the extension function:
     * ```kotlin
     *
     * simulation(AvailableIncarnations.SAPERE.incarnation(), environment) {
     *     // simulation configuration
     * }
     * ```
     *
     *
     * @see [AvailableIncarnations] for the DSL enum of available incarnations
     * @see [Incarnation] for the incarnation interface
     * @see [it.unibo.alchemist.boundary.dsl.Dsl.incarnation] for converting enum to instance
     */
    val incarnation: Incarnation<T, P>

    /**
     * The environment where the simulation takes place.
     *
     * @see [Environment]
     */
    val environment: Environment<T, P>

    /**
     * The launcher responsible for executing the simulation.
     *
     * Some implementations are available in [it.unibo.alchemist.boundary.launchers].
     *
     * @see [Launcher]
     */
    var launcher: Launcher

    /**
     * Random number generator controlling the evolution of the events of the simulation.
     *
     * @see [RandomGenerator]
     */
    var simulationGenerator: RandomGenerator

    /**
     * Random number generator controlling the position of random deployments.
     *
     * @see [RandomGenerator]
     */
    var scenarioGenerator: RandomGenerator

    /**
     * The network model (linking rule) that defines how nodes connect in the environment.
     *
     * @see [LinkingRule]
     */
    var networkModel: LinkingRule<T, P>

    /**
     * Configures node deployments for the simulation.
     *
     * ## Usage Example
     * ```kotlin
     * deployments {
     *    deploy(point(0,0))
     *    ...
     * }
     * ```
     *
     * @see [DeploymentsContextImpl] to configure deployments
     */
    context(environment: Environment<T, P>)
    fun deployments(
        block: DeploymentsContext<T, P>.() -> Unit,
    )

    /**
     * Adds a termination predicate to the simulation.
     *
     * @param terminator The termination predicate to add
     * @see [TerminationPredicate]
     */
    fun terminators(block: TerminatorsContext<T, P>.() -> Unit)

    /**
     * Adds an output monitor to the simulation.
     *
     * @param monitor The output monitor to add
     * @see [OutputMonitor]
     */
    fun monitors(block: OutputMonitorsContext<T, P>.() -> Unit)

    /**
     * Add an exporter to the simulation for data output.
     *
     * @param block The configuration block
     * @see [ExporterContextImpl]
     */
    fun exporter(block: ExporterContext<T, P>.() -> Unit)

    /**
     * Configures a global program.
     *
     * @param program the global reaction to add
     * @see [GlobalReaction]
     */
    fun programs(block: GlobalProgramsContext<T, P>.() -> Unit)

    /**
     * Schedules a block of code to execute later during the loading process.
     *
     * This is useful for debug purposes or for operations that need to be deferred
     *
     * Example:
     * ```kotlin
     * runLater {
     *     environment.nodes.forEach { node ->
     *         println("Node: ${node}")
     *     }
     * }
     * ```
     *
     * @param block The block of code to execute later
     */
    fun runLater(block: context(SimulationContext<T, P>) () -> Unit)

    /**
     * Add a spatial layer for a molecule.
     *
     * It is possible to define overlays (layers) of data that can be sensed
     * everywhere in the environment
     *
     * @param block The configuration block
     * @see [LayerContextImpl]
     */
    fun layer(block: LayerContext<T, P>.() -> Unit)

    /**
     * Registers a Linear Variable for batch simulations.
     *
     * Example usage with a range variable:
     * ```kotlin
     * var myParam by variable(RangeVariable(0.0, 10.0, 0.5))
     * ```
     *
     * @param source The variable source that provides the range of values
     * @see [Variable]
     */
    fun <A : Serializable> variable(source: Variable<out A>): VariablesContext.VariableProvider<A>

    /**
     * Registers a dependent variable that is computed from other variables.
     *
     * Example usage::
     * ```kotlin
     * var param by variable(RangeVariable(0.0, 10.0, 0.5))
     * var computedParam by variable { param * 2.0 }
     * ```
     *
     * @param source A function that computes the variable value
     */
    fun <A : Serializable> variable(source: () -> A): VariablesContext.DependentVariableProvider<A>
}
