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
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Position
import java.io.Serializable
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

/**
 * Main context for building and configuring a simulation.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position.
 */

class SimulationContextImpl<T, P : Position<P>>(override val incarnation: Incarnation<T, P>) : SimulationContext<T, P> {
    /** The environment instance (internal use). */
    var envIstance: Environment<T, P>? = null
    override val environment: Environment<T, P>
        get() = requireNotNull(envIstance) { "Environment has not been initialized yet" }

    /**
     * List of build steps to execute.
     */
    val buildSteps: MutableList<SimulationContextImpl<T, P>.() -> Unit> = mutableListOf()

    /**
     * List of output .
     */
    val monitors: MutableList<OutputMonitor<T, P>> = mutableListOf()

    /**
     * List of exporters.
     */
    val exporters: MutableList<ExporterContextImpl<T, P>> = mutableListOf()

    override var launcher: Launcher = DefaultLauncher()

    /**
     * Map of variable references.
     */
    val references: MutableMap<String, Any> = mutableMapOf()

    private var _scenarioGenerator: RandomGenerator? = null
    private var _simulationGenerator: RandomGenerator? = null

    override var scenarioGenerator: RandomGenerator
        get() {
            return _scenarioGenerator ?: MersenneTwister(0L).also { _scenarioGenerator = it }
        }
        set(value) {
            buildSteps.add { this._scenarioGenerator = value }
        }

    override var simulationGenerator: RandomGenerator
        get() {
            return _simulationGenerator ?: MersenneTwister(0L).also { _simulationGenerator = it }
        }
        set(value) {
            buildSteps.add { this._simulationGenerator = value }
        }

    private val layers: MutableMap<String, Layer<T, P>> = HashMap()

    override var networkModel: LinkingRule<T, P>
        get() = environment.linkingRule
        set(value) {
            buildSteps.add { this.environment.linkingRule = value }
        }

    /**
     * The variables context for managing simulation variables.
     */
    val variablesContext = VariablesContext()

    /**
     * Build a fresh new simulation context instance, and applies
     * all the build steps to it.
     * To ensure that each instance has
     * its own variables spaces: check the [VariablesContext] documentation for more details.
     * @see [VariablesContext]
     */
    fun build(envInstance: Environment<T, P>, values: Map<String, *>): SimulationContextImpl<T, P> {
        val batchContext = SimulationContextImpl(incarnation)
        batchContext.envIstance = envInstance
        batchContext.variablesContext.variables += this.variablesContext.variables
        batchContext.variablesContext.dependentVariables += this.variablesContext.dependentVariables
        logger.debug("Binding variables to batchInstance: {}", values)
        this.variablesContext.addReferences(values)
        buildSteps.forEach { batchContext.apply(it) }
        return batchContext
    }

    override fun deployments(block: DeploymentsContext<T, P>.() -> Unit) {
        logger.debug("adding deployments block inside {}", this)
        buildSteps.add {
            logger.debug("Configuring deployments inside {}", this)
            DeploymentsContextImpl(this).apply(block)
        }
    }

    override fun terminators(block: TerminatorsContext<T, P>.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        buildSteps.add { TerminatorsContextImpl(this).block() }
    }

    override fun monitors(block: OutputMonitorsContext<T, P>.() -> Unit) {
        buildSteps.add { OutputMonitorsContextImpl(this).block() }
    }

    override fun exporter(block: ExporterContextImpl<T, P>.() -> Unit) {
        buildSteps.add { this.exporters.add(ExporterContextImpl(this).apply(block)) }
    }

    override fun programs(block: GlobalProgramsContext<T, P>.() -> Unit) {
        buildSteps.add { GlobalProgramsContextImpl(this).block() }
    }

    override fun runLater(block: context(SimulationContext<T, P>)() -> Unit) {
        buildSteps.add { block() }
    }

    override fun layer(block: LayerContext<T, P>.() -> Unit) {
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
            this.layers[moleculeName] = layer
            this.environment.addLayer(molecule, layer)
        }
    }

    override fun <A : Serializable> variable(source: Variable<out A>): VariablesContext.VariableProvider<A> =
        variablesContext.register(source)

    override fun <A : Serializable> variable(source: () -> A): VariablesContext.DependentVariableProvider<A> =
        variablesContext.dependent(source)
}
