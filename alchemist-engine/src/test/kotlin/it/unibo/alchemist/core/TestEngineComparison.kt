/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.util.DependencyUtils
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.conditions.MoleculeHasConcentration
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.jvm.optionals.getOrNull
import org.junit.jupiter.api.fail

class TestEngineComparison : FreeSpec({

    data class TraceEntry(val time: Double, val nodeId: Int, val molecule: String, val concentration: Double)

    class TraceMonitor<T, P : Position<out P>> : OutputMonitor<T, P> {
        val trace = CopyOnWriteArrayList<TraceEntry>()

        override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
            environment.nodes.forEach { node ->
                node.contents.forEach { (mol, conc) ->
                    trace.add(TraceEntry(time.toDouble(), node.id, mol.name, (conc as Number).toDouble()))
                }
            }
        }
    }

    fun prepareEnvironment(): Continuous2DEnvironment<Double> {
        val incarnation = BiochemistryIncarnation()
        val environment = Continuous2DEnvironment(incarnation)
        environment.linkingRule = NoLinks()
        environment.addTerminator { it.simulation.time > DoubleTime(10.0) }
        return environment
    }

    fun runAndTrace(
        setup: (Continuous2DEnvironment<Double>) -> Unit,
        engineFactory: (Environment<Double, *>) -> Simulation<Double, *>,
    ): List<TraceEntry> {
        val environment = prepareEnvironment()
        setup(environment)
        val monitor = TraceMonitor<Double, Position<*>>()

        @Suppress("UNCHECKED_CAST")
        val engine = engineFactory(environment as Environment<Double, *>) as Simulation<Double, Position<*>>
        engine.addOutputMonitor(monitor)

        engine.play()
        engine.run()

        engine.error.getOrNull()?.let { err ->
            fail { "Simulation failed with an error: ${err.message}: ${err.stackTraceToString()}" }
        }

        return monitor.trace
    }

    val a = Biomolecule("A")
    val b = Biomolecule("B")
    val c = Biomolecule("C")

    fun setupNode(env: Continuous2DEnvironment<Double>): GenericNode<Double> {
        val node = GenericNode(env)
        node.setConcentration(a, 1.0)
        node.setConcentration(b, 0.0)
        node.setConcentration(c, 0.0)
        return node
    }

    fun addReaction(node: Node<Double>, rate: Double, conditionMolecule: Molecule, action: () -> Unit) {
        DependencyUtils.SimpleReaction(node, DiracComb(rate)) {
            action()
        }.apply {
            conditions = listOf(MoleculeHasConcentration(node, conditionMolecule, 1.0))
            node.addReaction(this)
        }
    }

    "Compare Engine and ReactiveEngine" - {
        listOf(
            "Simple Chain: A -> B -> C" to { env: Continuous2DEnvironment<Double> ->
                val node = setupNode(env)
                addReaction(node, 1.0, a) {
                    node.setConcentration(a, 0.0)
                    node.setConcentration(b, 1.0)
                }
                addReaction(node, 1.0, b) {
                    node.setConcentration(b, 0.0)
                    node.setConcentration(c, 1.0)
                }
                env.addNode(node, env.makePosition(0, 0))
                Unit
            },
            "Simple Branching: A -> B, A -> C" to { env: Continuous2DEnvironment<Double> ->
                val node = setupNode(env)
                addReaction(node, 1.0, a) {
                    node.setConcentration(b, node.getConcentration(b) + 1.0)
                }
                addReaction(node, 0.5, a) {
                    node.setConcentration(c, node.getConcentration(c) + 1.0)
                }
                env.addNode(node, env.makePosition(0, 0))
                Unit
            },
            "Simple Feedback Loop: A -> B -> A" to { env: Continuous2DEnvironment<Double> ->
                val node = setupNode(env)
                addReaction(node, 1.0, a) {
                    node.setConcentration(a, 0.0)
                    node.setConcentration(b, 1.0)
                }
                addReaction(node, 1.0, b) {
                    node.setConcentration(b, 0.0)
                    node.setConcentration(a, 1.0)
                }
                env.addNode(node, env.makePosition(0, 0))
                Unit
            },
        ).forEach { (_, setup) ->
            val traceEngine = runAndTrace(setup) { Engine(it) }
            val traceReactive = runAndTrace(setup) { ReactiveEngine(it) }
            traceReactive shouldContainExactly traceEngine
        }
    }
})
