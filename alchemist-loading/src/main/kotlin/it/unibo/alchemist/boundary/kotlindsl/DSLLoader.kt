/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.environments.EmptyEnvironment
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import org.slf4j.LoggerFactory

internal class DSLLoader<T, P : Position<P>, I : Incarnation<T, P>>(
    private val incarnation: I,
    private val block: context(I) SimulationContext<T, P>.() -> Unit,
) : Loader {

    private val logger = LoggerFactory.getLogger("Alchemist Kotlin DSL Loader")
    override var constants: Map<String, Any?> = emptyMap()
    override val remoteDependencies: List<String> get() = TODO("Not yet implemented")
    override var launcher: Launcher = DefaultLauncher()
    override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
    override var variables: Map<String, Variable<*>> = emptyMap()
    val defaultEnvironment = getWithTyped(emptyMap<String, Nothing>())

    @Suppress("UNCHECKED_CAST")
    override fun <T, P : Position<P>> getWith(values: Map<String, *>): Simulation<T, P> =
        (if (values.isEmpty()) defaultEnvironment else getWithTyped(values)) as Simulation<T, P>

    private fun getWithTyped(values: Map<String, *>): Simulation<T, P> {
        logger.debug("Creating simulation with variables: {}", values)
        val loader = this
        var theEnvironment: Environment<T, P> = EmptyEnvironment(incarnation)
        val exporters = mutableListOf<Exporter<T, P>>()
        val monitors = mutableListOf<OutputMonitor<T, P>>()
        context(incarnation) {
            object : SimulationContext<T, P> {

                var launcherHasNotBeenSet = true
                var environmentHasNotBeenSet = true
                lateinit var simulationRNG: RandomGenerator
                lateinit var scenarioRNG: RandomGenerator

                override fun <E : Environment<T, P>> environment(
                    environment: E,
                    block: context(E) EnvironmentContext<T, P>.() -> Unit,
                ) {
                    if (!this::simulationRNG.isInitialized) {
                        simulationRNG = MersenneTwister(0L)
                    }
                    if (!this::scenarioRNG.isInitialized) {
                        scenarioRNG = MersenneTwister(0L)
                    }
                    check(environmentHasNotBeenSet) {
                        "Only one environment can be set, currently set: $theEnvironment"
                    }
                    theEnvironment = environment
                    environmentHasNotBeenSet = false
                    context(environment, simulationRNG) {
                        check(contextOf<RandomGenerator>() == simulationRNG)
                        object : EnvironmentContext<T, P> {
                            override fun deployments(block: context(RandomGenerator) DeploymentsContext.() -> Unit) {
                                check(contextOf<RandomGenerator>() == simulationRNG)
                                context(scenarioRNG) {
                                    object : DeploymentsContext {
                                        context(randomGenerator: RandomGenerator, environment: Environment<T, P>)
                                        override fun <T, P : Position<P>> deploy(
                                            deployment: Deployment<P>,
                                            nodeFactory: (P) -> Node<T>,
                                            block: context(RandomGenerator, Node<T>) DeploymentContext<T, P>.() -> Unit,
                                        ) {
                                            check(contextOf<RandomGenerator>() == scenarioRNG)
                                            deployment.forEach { position ->
                                                context(simulationRNG) {
                                                    check(contextOf<RandomGenerator>() == simulationRNG)
                                                    val node: Node<T> = nodeFactory(position)
                                                    context(node) {
                                                        object : DeploymentContext<T, P> {
                                                            override val position: P get() = position
                                                        }.block()
                                                    }
                                                    environment.addNode(node, position)
                                                }
                                            }
                                        }
                                    }.block()
                                }
                            }
                        }.block()
                    }
                }

                override fun exportWith(exporter: Exporter<T, P>, block: ExporterContext<T, P>.() -> Unit) {
                    val extractors = mutableListOf<Extractor<*>>()
                    object : ExporterContext<T, P> {
                        override fun Extractor<*>.unaryMinus() {
                            extractors += this
                        }
                    }.block()
                    exporter.bindDataExtractors(extractors)
                    exporters += exporter
                }

                override fun scenarioRandomGenerator(randomGenerator: RandomGenerator) {
                    checkSeedCanBeSet()
                    scenarioRNG = randomGenerator
                }

                override fun simulationRandomGenerator(randomGenerator: RandomGenerator) {
                    checkSeedCanBeSet()
                    simulationRNG = randomGenerator
                }

                override fun monitor(monitor: OutputMonitor<T, P>) {
                    monitors += monitor
                }

                override fun launcher(launcher: Launcher) {
                    check(launcherHasNotBeenSet) {
                        "Only one launcher can be set, currently set: ${loader.launcher}"
                    }
                    launcherHasNotBeenSet = false
                    loader.launcher = launcher
                }

                override fun <V : Serializable> variable(variable: Variable<out V>): ReadOnlyProperty<Any?, V> =
                    ReadOnlyProperty { thisRef, property ->
                        variables += property.name to variable
                        @Suppress("UNCHECKED_CAST")
                        values.getOrDefault(property.name, variable.default) as V
                    }

                fun checkSeedCanBeSet() {
                    check(environmentHasNotBeenSet) {
                        "Seeds must be set before the environment is defined to preserve reproducibility"
                    }
                }
            }.block()
        }
        check(variables.keys.containsAll(values.keys)) {
            val undefinedVariables = values.keys - variables.keys
            """
            The following variables provided in input have no corresponding variable defined in the simulation context:
            $undefinedVariables
            The available variables are: ${variables.keys}
            """.trimIndent()
        }
        val theSimulation: Simulation<T, P> = Engine(theEnvironment)
        if (exporters.isNotEmpty()) {
            theSimulation.addOutputMonitor(GlobalExporter(exporters))
        }
        monitors.forEach { monitor -> theSimulation.addOutputMonitor(monitor) }
        return theSimulation
    }
}
