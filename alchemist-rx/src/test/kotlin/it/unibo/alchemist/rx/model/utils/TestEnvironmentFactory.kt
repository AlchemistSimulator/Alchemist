/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.utils

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.ArrayIndexedPriorityQueue
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.terminators.StepCount
import it.unibo.alchemist.rx.core.ReactiveEngine
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment.Companion.asObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.test.AlchemistTesting.createEmptyEnvironment
import java.util.Random
import org.apache.commons.math3.random.RandomGeneratorFactory

object TestEnvironmentFactory {

    private val rnd = RandomGeneratorFactory.createRandomGenerator(Random(123))

    private val testIncarnation = SupportedIncarnations.getAvailableIncarnations().first().let { name ->
        SupportedIncarnations.get<Double, Euclidean2DPosition>(name).orElseThrow {
            IllegalStateException("Canno find incarnation \"$name\"")
        }
    }

    /**
     * Runs the give [body] with a test [ObservableEnvironment]. If [withEngine] is set to true,
     * the given [body] will be run with the [createEmptyEnvironment] environment, i.e. environment
     * handling is performed within the simulation thread. If set to false, a simple
     * [Continuous2DEnvironment] is created.
     * In both cases neighborhoods are connected to each other by means of [ConnectWithinDistance]
     * linking rule with the [radius][neighborhoodRadius] given as input.
     *
     * > Note: the incarnation used is [Biochemistry][it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation].
     *
     * @param withEngine whether the given [body] has to be executed in the simulation thread.
     * @param neighborhoodRadius the radius of [ConnectWithinDistance] linking rule.
     * @param body the actual test body to be executed with the test [ObservableEnvironment].
     */
    fun withObservableTestEnvironment(
        withEngine: Boolean = false,
        neighborhoodRadius: Double = 1.5,
        body: ObservableEnvironment<Double, Euclidean2DPosition>.() -> Unit,
    ) {
        if (withEngine) {
            val baseEnv = createEmptyEnvironment<Double>().apply {
                linkingRule = ConnectWithinDistance(neighborhoodRadius)
            }
            with(baseEnv.simulation) {
                addOutputMonitor(object : OutputMonitor<Double, Euclidean2DPosition> {
                    override fun initialized(environment: Environment<Double, Euclidean2DPosition>) {
                        environment.asObservableEnvironment().body()
                    }
                })
                play()
                run()
            }
        } else {
            Continuous2DEnvironment(testIncarnation).asObservableEnvironment().apply {
                linkingRule = ConnectWithinDistance(neighborhoodRadius)
            }.body()
        }
    }

    /**
     * Runs a simple simulation with a [ReactiveEngine]. You can set up the simulation and the environment
     * in the [setup] block, then you can perform checks after execution has terminated with [onFinishChecks]
     * block.
     *
     * @param setup the code block to setup this simulation environment
     * @param onFinishChecks checks to perform on the environment when this simulation finishes
     */
    fun withReactiveEngine(
        setup: ObservableEnvironment<Double, Euclidean2DPosition>.() -> Unit,
        onFinishChecks: ObservableEnvironment<Double, Euclidean2DPosition>.() -> Unit,
    ) {
        val env = Continuous2DEnvironment(testIncarnation).asObservableEnvironment()
        env.addTerminator(StepCount(100))

        with(ReactiveEngine(env, ArrayIndexedPriorityQueue())) {
            addOutputMonitor(object : OutputMonitor<Double, Euclidean2DPosition> {
                override fun initialized(environment: Environment<Double, Euclidean2DPosition>) {
                    environment.asObservableEnvironment().setup()
                }

                override fun finished(environment: Environment<Double, Euclidean2DPosition>, time: Time, step: Long) {
                    environment.asObservableEnvironment().onFinishChecks()
                }
            })
            play()
            run()
        }
    }

    fun <T> ObservableEnvironment<T, Euclidean2DPosition>.spawnNode(vararg coordinates: Double): ObservableNode<T> =
        rxIncarnation.createNode(rnd, this, null)
            .also { addNode(it, makePosition(coordinates)) }

    fun getObservableEnvironment(): ObservableEnvironment<Double, Euclidean2DPosition> =
        createEmptyEnvironment<Double>().asObservableEnvironment()

    fun Pair<Double, Double>.toPosition(): Euclidean2DPosition = Euclidean2DPosition(first, second)
}
