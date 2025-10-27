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

class SimulationContext<T, P : Position<P>>(val incarnation: Incarnation<T, P>, val environment: Environment<T, P>) {
    val buildSteps: MutableList<() -> Unit> = mutableListOf()
    val monitors: MutableList<OutputMonitor<T, P>> = mutableListOf()
    val exporters: MutableList<ExporterContext<T, P>> = mutableListOf()
    var launcher: Launcher = DefaultLauncher()
    var scenarioGenerator: RandomGenerator = MersenneTwister(0L)
    var simulationGenerator: RandomGenerator = MersenneTwister(0L)

    // deploymemt -> node -> program (time distribution -> actionable ( simulation))
    private val layers: MutableMap<String, Layer<T, P>> = HashMap()
    private var _networkModel: LinkingRule<T, P> = NoLinks()
    var networkModel: LinkingRule<T, P>
        get() = _networkModel
        set(value) {
            _networkModel = value
            environment.linkingRule = value
        }
    val variablesContext = VariablesContext()
    fun build() {
        buildSteps.forEach { it() }
    }

    fun deployments(block: DeploymentsContext<T, P>.() -> Unit) {
        buildSteps.add { DeploymentsContext(this).apply(block) }
    }
    fun addTerminator(predicate: TerminationPredicate<*, *>) {
        @Suppress("UNCHECKED_CAST")
        buildSteps.add { environment.addTerminator(predicate as TerminationPredicate<T, P>) }
    }
    fun addMonitor(monitor: OutputMonitor<T, P>) {
        buildSteps.add { monitors.add(monitor) }
    }
    fun exporter(block: ExporterContext<T, P>.() -> Unit) {
        buildSteps.add { exporters.add(ExporterContext(this).apply(block)) }
    }
    fun program(program: GlobalReaction<T>) {
        buildSteps.add { this.environment.addGlobalReaction(program) }
    }
    fun runLater(block: () -> Unit) {
        buildSteps.add { block() }
    }
    fun layer(block: LayerContext<T, P>.() -> Unit) {
        buildSteps.add {
            val l = LayerContext(this).apply(block)
            require(l.layer != null) { "Layer must be specified" }
            require(l.molecule != null) { "Molecule must be specified" }
            require(!this.layers.containsKey(l.molecule)) {
                "Inconsistent layer definition for molecule ${l.molecule}. " +
                    "There must be a single layer per molecule"
            }
            val molecule = incarnation.createMolecule(l.molecule)
            layers[l.molecule!!] = l.layer!!
            environment.addLayer(molecule, l.layer!!)
        }
    }
    fun <T : Serializable> variable(source: Variable<out T>): VariablesContext.VariableProvider<T> =
        variablesContext.register(source)
    fun <T : Serializable> variable(source: () -> T): VariablesContext.DependentVariableProvider<T> =
        variablesContext.dependent(source)
}
