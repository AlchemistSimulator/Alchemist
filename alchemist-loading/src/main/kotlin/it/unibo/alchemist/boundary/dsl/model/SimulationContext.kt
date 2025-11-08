/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("UNCHECKED_CAST")

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.linkingrules.NoLinks
import java.io.Serializable
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

/**
 * Main context for building and configuring a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 * @param incarnation The incarnation instance.
 * @param environment The environment instance.
 */
class SimulationContext<T, P : Position<P>>(val incarnation: Incarnation<T, P>, val environment: Environment<T, P>) {
    /**
     * List of build steps to execute.
     */
    val buildSteps: MutableList<() -> Unit> = mutableListOf()

    /**
     * List of output monitors.
     */
    val monitors: MutableList<OutputMonitor<T, P>> = mutableListOf()

    /**
     * List of exporters.
     */
    val exporters: MutableList<ExporterContext<T, P>> = mutableListOf()

    /**
     * The launcher for the simulation.
     */
    var launcher: Launcher = DefaultLauncher()

    /**
     * The random generator for scenario generation.
     */
    var scenarioGenerator: RandomGenerator = MersenneTwister(0L)

    /**
     * The random generator for simulation execution.
     */
    var simulationGenerator: RandomGenerator = MersenneTwister(0L)

    private val layers: MutableMap<String, Layer<T, P>> = HashMap()
    private var _networkModel: LinkingRule<T, P> = NoLinks()

    /**
     * The network model (linking rule) for the environment.
     */
    var networkModel: LinkingRule<T, P>
        get() = _networkModel
        set(value) {
            _networkModel = value
            environment.linkingRule = value
        }

    /**
     * The variables context for managing simulation variables.
     */
    val variablesContext = VariablesContext()

    /**
     * Executes all build steps.
     */
    fun build() {
        buildSteps.forEach { it() }
    }

    /**
     * Configures deployments for the simulation.
     *
     * @param block The deployments configuration block.
     */
    fun deployments(block: DeploymentsContext<T, P>.() -> Unit) {
        buildSteps.add { DeploymentsContext(this).apply(block) }
    }

    /**
     * Adds a termination predicate to the simulation.
     *
     * @param predicate The termination predicate.
     */
    fun addTerminator(predicate: TerminationPredicate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        buildSteps.add { environment.addTerminator(predicate as TerminationPredicate<T, P>) }
    }

    /**
     * Adds an output monitor to the simulation.
     *
     * @param monitor The output monitor.
     */
    fun addMonitor(monitor: OutputMonitor<T, P>) {
        buildSteps.add { monitors.add(monitor) }
    }

    /**
     * Configures an exporter for the simulation.
     *
     * @param block The exporter configuration block.
     */
    fun exporter(block: ExporterContext<T, P>.() -> Unit) {
        buildSteps.add { exporters.add(ExporterContext<T, P>().apply(block)) }
    }

    /**
     * Adds a global reaction to the simulation.
     *
     * @param program The global reaction.
     */
    fun program(program: GlobalReaction<T>) {
        buildSteps.add { this.environment.addGlobalReaction(program) }
    }

    /**
     * Schedules a block to run later during build.
     *
     * @param block The block to execute.
     */
    fun runLater(block: () -> Unit) {
        buildSteps.add { block() }
    }

    /**
     * Configures a layer for the simulation.
     *
     * @param block The layer configuration block.
     */
    fun layer(block: LayerContext<T, P>.() -> Unit) {
        buildSteps.add {
            val l = LayerContext<T, P>().apply(block)
            val layer = requireNotNull(l.layer) { "Layer must be specified" }
            val moleculeName = requireNotNull(l.molecule) { "Molecule must be specified" }
            require(!this.layers.containsKey(moleculeName)) {
                "Inconsistent layer definition for molecule $moleculeName. " +
                    "There must be a single layer per molecule"
            }
            val molecule = incarnation.createMolecule(moleculeName)
            layers[moleculeName] = layer
            environment.addLayer(molecule, layer)
        }
    }

    /**
     * Registers a variable in the variables context.
     *
     * @param source The variable source.
     * @return A variable provider for property delegation.
     */
    fun <T : Serializable> variable(source: Variable<out T>): VariablesContext.VariableProvider<T> =
        variablesContext.register(source)

    /**
     * Registers a dependent variable in the variables context.
     *
     * @param source The function that provides the variable value.
     * @return A dependent variable provider for property delegation.
     */
    fun <T : Serializable> variable(source: () -> T): VariablesContext.DependentVariableProvider<T> =
        variablesContext.dependent(source)
}
