/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.engine

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.ArrayIndexedPriorityQueue
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.conditions.MoleculeHasConcentration
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.ChemicalReaction
import it.unibo.alchemist.model.terminators.StepCount
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.rx.core.ReactiveEngine
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment.Companion.asObservableEnvironment
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import java.util.Random
import java.util.concurrent.CopyOnWriteArrayList
import org.apache.commons.math3.random.RandomGeneratorFactory

class ReactiveEngineCorrectnessTest : FunSpec({

    data class EventTrace(val step: Long, val time: Double, val nodeId: Int, val description: String)

    fun runSimulation(seed: Long, steps: Long, useReactive: Boolean): List<EventTrace> {
        val rng = RandomGeneratorFactory.createRandomGenerator(Random(seed))
        val env = Continuous2DEnvironment(TestEnvironmentFactory.testIncarnation).asObservableEnvironment().apply {
            linkingRule = ConnectWithinDistance(1.5)
        }

        val molA = Molecule { "A" }
        val molB = Molecule { "B" }

        val node1 = env.spawnNode(0.0, 0.0)
        node1.setConcentration(molA, 10.0)

        val node2 = env.spawnNode(1.0, 0.0)

        ChemicalReaction(node1, ExponentialTime(1.0, rng)).apply {
            conditions = listOf(MoleculeHasConcentration(node1, molA, 0.1))
            actions = listOf(object : AbstractAction<Double>(node1) {
                override fun execute() {
                    val a = getConcentration(molA).orElse(0.0)
                    if (a > 0) {
                        setConcentration(molA, a - 1.0)
                        setConcentration(molB, getConcentration(molB).orElse(0.0) + 1.0)
                    }
                }
                override fun getContext() = Context.LOCAL
                override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>) = this
                override fun toString() = "R1"
            })

            node1.addReaction(this)
        }

        ChemicalReaction(node2, ExponentialTime(1.0, rng)).apply {
            conditions = listOf(object : AbstractCondition<Double>(node2) {
                override fun getContext() = Context.NEIGHBORHOOD
                override fun getPropensityContribution() = 1.0
                override fun isValid(): Boolean = env.getNeighborhood(node2).neighbors.any {
                    it.getConcentration(molB) > 5.0
                }
                init {
                    declareDependencyOn(molB)
                }
                override fun cloneCondition(node: Node<Double>, reaction: Reaction<Double>) = this
            })
            actions = listOf(object : AbstractAction<Double>(node2) {
                override fun execute() {
                    setConcentration(molB, 1.0)
                }
                override fun getContext() = Context.LOCAL
                override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>) = this
                override fun toString() = "R2"
            })
            node2.addReaction(this)
        }

        env.addTerminator(StepCount(steps))

        val trace = CopyOnWriteArrayList<EventTrace>()
        val monitor = object : OutputMonitor<Double, Euclidean2DPosition> {
            override fun stepDone(
                environment: Environment<Double, Euclidean2DPosition>,
                reaction: Actionable<Double>?,
                time: Time,
                step: Long,
            ) {
                if (reaction is Reaction) {
                    trace.add(EventTrace(step, time.toDouble(), reaction.node.id, reaction.actions.first().toString()))
                }
            }
        }

        val scheduler = ArrayIndexedPriorityQueue<Double>()
        val engine = if (useReactive) ReactiveEngine(env, scheduler) else Engine(env, scheduler)

        engine.addOutputMonitor(monitor)
        engine.play()
        engine.run()

        if (engine.error.isPresent) throw engine.error.get()

        return trace
    }

    test("ReactiveEngine should produce the same trace as standard Engine for a deterministic scenario") {
        val seed = 1926L
        val steps = 50L

        val standardTrace = runSimulation(seed, steps, useReactive = false)
        val reactiveTrace = runSimulation(seed, steps, useReactive = true)

        standardTrace.size shouldBe reactiveTrace.size
        standardTrace.zip(reactiveTrace).forEach { (std, rx) -> std shouldBe rx }
    }
})
