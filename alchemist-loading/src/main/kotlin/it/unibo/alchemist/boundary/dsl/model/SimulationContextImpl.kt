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
import it.unibo.alchemist.boundary.dsl.util.LoadingSystemLogger.logger
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
 */

class SimulationContextImpl<T, P : Position<P>>(
    override val incarnation: Incarnation<T, P>,
    override val environment: Environment<T, P>,
) : SimulationContext<T, P> {
    /**
     * List of build steps to execute.
     */
    val buildSteps: MutableList<() -> Unit> = mutableListOf()

    /**
     * List of output .
     */
    val monitors: MutableList<OutputMonitor<T, P>> = mutableListOf()

    /**
     * List of exporters.
     */
    val exporters: MutableList<ExporterContextImpl<T, P>> = mutableListOf()

    override var launcher: Launcher = DefaultLauncher()

    override var scenarioGenerator: RandomGenerator = MersenneTwister(0L)

    override var simulationGenerator: RandomGenerator = MersenneTwister(0L)

    private val layers: MutableMap<String, Layer<T, P>> = HashMap()
    private var _networkModel: LinkingRule<T, P> = NoLinks()

    override var networkModel: LinkingRule<T, P>
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

    override fun deployments(block: DeploymentsContextImpl<T, P>.() -> Unit) {
        buildSteps.add { DeploymentsContextImpl(this).apply(block) }
    }

    override fun addTerminator(terminator: TerminationPredicate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        buildSteps.add { environment.addTerminator(terminator as TerminationPredicate<T, P>) }
    }

    override fun addMonitor(monitor: OutputMonitor<T, P>) {
        buildSteps.add { monitors.add(monitor) }
    }

    override fun exporter(block: ExporterContextImpl<T, P>.() -> Unit) {
        buildSteps.add { exporters.add(ExporterContextImpl<T, P>().apply(block)) }
    }

    override fun program(program: GlobalReaction<T>) {
        buildSteps.add { this.environment.addGlobalReaction(program) }
    }

    override fun runLater(block: () -> Unit) {
        buildSteps.add { block() }
    }

    override fun layer(block: LayerContextImpl<T, P>.() -> Unit) {
        buildSteps.add {
            val l = LayerContextImpl<T, P>().apply(block)
            val layer = requireNotNull(l.layer) { "Layer must be specified" }
            val moleculeName = requireNotNull(l.molecule) { "Molecule must be specified" }
            require(!this.layers.containsKey(moleculeName)) {
                "Inconsistent layer definition for molecule $moleculeName. " +
                    "There must be a single layer per molecule"
            }
            val molecule = incarnation.createMolecule(moleculeName)
            logger.debug("Adding layer for molecule {}: {}", moleculeName, layer)
            layers[moleculeName] = layer
            environment.addLayer(molecule, layer)
        }
    }

    override fun <T : Serializable> variable(source: Variable<out T>): VariablesContext.VariableProvider<T> =
        variablesContext.register(source)

    override fun <T : Serializable> variable(source: () -> T): VariablesContext.DependentVariableProvider<T> =
        variablesContext.dependent(source)
}
