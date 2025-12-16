/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.toPosition
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withObservableTestEnvironment

class ObservableEnvironmentTest : FunSpec({

    context("test nodes in environment") {

        test("environment nodes should be observable") {
            withObservableTestEnvironment {
                val molecule = Molecule { "a" }
                val node = spawnNode(0.0, 0.0).apply { setConcentration(molecule, 0.0) }

                var countConcentrationChanges = 0
                observeNode(node).observeConcentration(molecule).onChange(this@test) { countConcentrationChanges++ }

                repeat(10) {
                    getNodeByID(node.id).setConcentration(molecule, it.toDouble())
                }
                countConcentrationChanges shouldBe 10
            }
        }

        test("nodes' positions in the environment should be observable") {
            withObservableTestEnvironment {
                val node = spawnNode(0.0, 0.0)
                var countPositionChanged = -1
                observeNodePosition(node).onChange(this) { countPositionChanged++ }

                repeat(10) {
                    moveNodeToPosition(node, (1.0 + it.toDouble() to 0.0).toPosition())
                }

                countPositionChanged shouldBe 10
                getPosition(node) shouldBe Pair(10.0, 0.0).toPosition()

                countPositionChanged = 0

                // moving in same position should not trigger updates
                repeat(5) {
                    moveNodeToPosition(node, getPosition(node))
                }

                countPositionChanged shouldBe 0
            }
        }

        test("environment nodes count should be observable") {
            withObservableTestEnvironment {
                var nodeCount = -1
                observeNodeCounts().onChange(this) { nodeCount++ }
                val n1 = spawnNode(0.0, 0.0)
                val n2 = spawnNode(1.0, 0.0)
                val n3 = spawnNode(2.0, 2.0)

                nodeCount shouldBe 3

                var lastCount = -1
                observeNodeCounts().onChange(this) { lastCount = it }

                lastCount shouldBe 3

                removeNode(n1)
                lastCount shouldBe 2

                removeNode(n2)
                lastCount shouldBe 1

                removeNode(n3)
                lastCount shouldBe 0
            }
        }

        test("observeAnyMovement should emit when positions of any node change") {
            withObservableTestEnvironment {
                val n1 = spawnNode(0.0, 0.0)
                val n2 = spawnNode(1.0, 0.0)

                val movementObservable = observeAnyMovement()

                val seen = mutableListOf<Map<Int, Euclidean2DPosition>>()
                movementObservable.onChange(this) { map ->
                    seen += map.mapValues { it.value }
                }

                seen.size shouldBe 1
                seen.last().keys shouldContainExactlyInAnyOrder listOf(n1.id, n2.id)

                moveNodeToPosition(n1, (2.0 to 0.0).toPosition())
                moveNodeToPosition(n2, (3.0 to 0.0).toPosition())

                seen.size shouldBe 3
                seen.last().keys shouldContainExactlyInAnyOrder listOf(n1.id, n2.id)
                seen.last()[n1.id] shouldBe (2.0 to 0.0).toPosition()
                seen.last()[n2.id] shouldBe (3.0 to 0.0).toPosition()
            }
        }

        test("observeNodes should track node additions and removals") {
            withObservableTestEnvironment {
                val nodesObservable = observeNodes()
                var currentNodes = emptySet<Int>()
                nodesObservable.onChange(this) { set ->
                    currentNodes = set.map { it.id }.toSet()
                }

                val n1 = spawnNode(0.0, 0.0)
                currentNodes shouldContainExactlyInAnyOrder listOf(n1.id)

                val n2 = spawnNode(1.0, 1.0)
                currentNodes shouldContainExactlyInAnyOrder listOf(n1.id, n2.id)

                removeNode(n1)
                currentNodes shouldContainExactlyInAnyOrder listOf(n2.id)
            }
        }
    }

    test("observeNeighborhood should reflect neighbors according to linking rule") {
        withObservableTestEnvironment(neighborhoodRadius = 1.5) {
            val a = spawnNode(0.0, 0.0)
            val b = spawnNode(1.0, 0.0)
            val c = spawnNode(3.0, 0.0)

            val neighObsA = observeNeighborhood(a)

            val snapshots = mutableListOf<List<Int>>()
            neighObsA.onChange(this) { opt ->
                opt.getOrNull()
                    ?.let { obsNeigh -> snapshots += obsNeigh.neighbors.map { it.id } }
                    ?: snapshots.add(emptyList())
            }

            moveNodeToPosition(c, (0.0 to 1.0).toPosition())
            snapshots.last() shouldContainExactlyInAnyOrder listOf(b.id, c.id)
            moveNodeToPosition(b, (100.0 to 0.0).toPosition())
            snapshots.last() shouldContainExactlyInAnyOrder listOf(c.id)
        }
    }
})
