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
import it.unibo.alchemist.core.ArrayIndexedPriorityQueue
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.actions.SetLocalMoleculeConcentration
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.conditions.MoleculeHasConcentration
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.reactions.ChemicalReaction
import it.unibo.alchemist.model.terminators.StepCount
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.rx.core.ReactiveEngine
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment.Companion.asObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withReactiveEngine
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@Suppress("AssignedValueIsNeverRead")
class ReactiveEngineTest : FunSpec({
    test("simple engine execution test") {
        lateinit var node: ObservableNode<Double>
        val molecule = Molecule { "A" }
        var r2Executed = false

        withReactiveEngine({
            node = spawnNode(0.0, 0.0)
            val reaction1 = ChemicalReaction(node, DiracComb(1.0)).apply {
                actions = listOf(SetLocalMoleculeConcentration(node, molecule, 1.0))
            }

            val reaction2 = ChemicalReaction(node, DiracComb(2.0)).apply {
                conditions = listOf(MoleculeHasConcentration(node, molecule, 1.0))
                actions = listOf(object : AbstractAction<Double>(node) {
                    override fun execute() {
                        r2Executed = true
                    }

                    override fun getContext(): Context = Context.LOCAL

                    override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>): Action<Double> {
                        TODO("Not yet implemented")
                    }
                })
            }

            node.addReaction(reaction1)
            node.addReaction(reaction2)
        }) {
            node.contains(molecule) shouldBe true
            r2Executed shouldBe true
        }
    }

    test("neighborhood dependency execution test") {
        lateinit var node1: ObservableNode<Double>
        lateinit var node2: ObservableNode<Double>

        val molecule = Molecule { "A" }
        var r2Executed = false

        withReactiveEngine({
            node1 = spawnNode(0.0, 0.0)
            node2 = spawnNode(1.0, 0.0)

            ChemicalReaction(node1, DiracComb(1.0)).apply {
                actions = listOf(SetLocalMoleculeConcentration(node1, molecule, 1.0))
                node1.addReaction(this)
            }

            ChemicalReaction(node2, DiracComb(2.0)).apply {
                conditions = listOf(object : AbstractCondition<Double>(node2) {
                    override fun getContext() = Context.NEIGHBORHOOD
                    override fun getPropensityContribution() = 1.0
                    override fun isValid(): Boolean = getNeighborhood(node2).neighbors.any {
                        it.getConcentration(molecule) == 1.0
                    }
                    init {
                        declareDependencyOn(molecule)
                    }
                })

                actions = listOf(object : AbstractAction<Double>(node2) {
                    override fun execute() {
                        r2Executed = true
                    }
                    override fun getContext() = Context.LOCAL
                    override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>) = this
                })
                node2.addReaction(this)
            }
        }) {
            node1.contains(molecule) shouldBe true
            r2Executed shouldBe true
        }
    }

    test("movement triggers neighobrhood reactions") {
        lateinit var node1: ObservableNode<Double>
        lateinit var node2: ObservableNode<Double>
        val molecule = Molecule { "A" }
        var r2Executed = false

        withReactiveEngine({
            node1 = spawnNode(0.0, 0.0)
            ChemicalReaction(node1, DiracComb(1.0)).apply {
                actions = listOf(SetLocalMoleculeConcentration(node1, molecule, 1.0))
                node1.addReaction(this)
            }

            node2 = spawnNode(10.0, 0.0)
            ChemicalReaction(node2, DiracComb(1.0)).apply {
                conditions = listOf(object : AbstractCondition<Double>(node2) {
                    override fun getContext() = Context.NEIGHBORHOOD
                    override fun getPropensityContribution(): Double = 1.0
                    override fun isValid(): Boolean = getNeighborhood(node2).neighbors.any {
                        it.getConcentration(molecule) == 1.0
                    }
                    init {
                        declareDependencyOn(molecule)
                    }
                })

                actions = listOf(object : AbstractAction<Double>(node2) {
                    override fun execute() {
                        r2Executed = true
                    }
                    override fun getContext(): Context = Context.LOCAL
                    override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>): Action<Double> = this
                })
                node2.addReaction(this)
            }

            ChemicalReaction(node2, DiracComb(0.5)).apply {
                actions = listOf(object : AbstractAction<Double>(node2) {
                    override fun execute() {
                        moveNodeToPosition(node2, makePosition(1.0, 0.0))
                    }
                    override fun getContext(): Context = Context.LOCAL
                    override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>): Action<Double> = this
                })
                node2.addReaction(this)
            }

            node2.reactions.firstOrNull()?.canExecute() shouldBe false
        }) {
            r2Executed shouldBe true
        }
    }

    @Suppress("UNCHECKED_CAST")
    test("batch updates should trigger single reschedule") {
        val scheduler = spy(ArrayIndexedPriorityQueue<Double>())
        lateinit var node1: ObservableNode<Double>
        lateinit var node2: ObservableNode<Double>
        val molA = Molecule { "A" }
        val molB = Molecule { "B" }

        val env = Continuous2DEnvironment(TestEnvironmentFactory.testIncarnation).asObservableEnvironment().apply {
            linkingRule = ConnectWithinDistance(1.5)
        }
        env.addTerminator(StepCount(1))

        val engine = ReactiveEngine(env, scheduler)

        node1 = env.spawnNode(0.0, 0.0)
        node2 = env.spawnNode(1.0, 0.0)

        ChemicalReaction(node1, DiracComb(2.0)).apply {
            actions = listOf(object : AbstractAction<Double>(node1) {
                override fun execute() {
                    node1.setConcentration(molA, 1.0)
                    node1.setConcentration(molB, 1.0)
                }

                override fun getContext() = Context.LOCAL
                override fun cloneAction(node: Node<Double>, reaction: Reaction<Double>) = this
            })
            node1.addReaction(this)
        }

        ChemicalReaction(node2, TestEnvironmentFactory.testExponentialTimeDistribution()).apply {
            conditions = listOf(object : AbstractCondition<Double>(node2) {
                override fun getContext() = Context.NEIGHBORHOOD

                override fun getPropensityContribution(): Double {
                    val neighbors = env.getNeighborhood(node2).neighbors
                    val concA = neighbors.sumOf { it.getConcentration(molA) ?: 0.0 }
                    val concB = neighbors.sumOf { it.getConcentration(molB) ?: 0.0 }
                    return 1.0 + concA + concB
                }

                override fun isValid(): Boolean = true

                init {
                    declareDependencyOn(molA)
                    declareDependencyOn(molB)
                }
            })
            node2.addReaction(this)
        }

        engine.play()
        engine.run()

        val captor = ArgumentCaptor.forClass(Actionable::class.java) as ArgumentCaptor<*>
        verify(scheduler, atLeast(0)).updateReaction(captor.capture() as Actionable<Double?>?)

        val r2Updates = captor.allValues.filter {
            (it as? Reaction<Double>)?.node?.id == node2.id
        }
        r2Updates.size shouldBe 1
    }
})
