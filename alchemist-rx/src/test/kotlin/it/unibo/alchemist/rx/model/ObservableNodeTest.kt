/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import arrow.core.Option
import arrow.core.some
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.rx.model.adapters.ObservableNode.NodeExtension.asObservableNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withObservableTestEnvironment

class ObservableNodeTest : FunSpec({
    context("wrapping and basic behavior") {

        test("asObservableNode should be idempotent") {
            withObservableTestEnvironment {
                val node = spawnNode(0.0, 0.0)
                val obs = node.asObservableNode()
                node shouldBe obs
            }
        }

        test("observableContents should reflect initial node contents") {
            withObservableTestEnvironment {
                val molA = Molecule { "A" }
                val molB = Molecule { "B" }
                val node = spawnNode(0.0, 0.0).apply {
                    setConcentration(molA, 1.0)
                    setConcentration(molB, 2.0)
                }

                node.observableContents.asMap().keys shouldContainExactlyInAnyOrder listOf(molA, molB)
                node.observableContents.asMap()[molA] shouldBe 1.0
                node.observableContents.asMap()[molB] shouldBe 2.0
            }
        }
    }

    context("observableMoleculeCount") {

        test("observableMoleculeCount should emit molecule count changes") {
            withObservableTestEnvironment {
                val molA = Molecule { "A" }
                val molB = Molecule { "B" }

                val node = spawnNode(0.0, 0.0)

                var emissions = 0
                var lastCount = -1
                node.observableMoleculeCount.onChange(this) {
                    emissions++
                    lastCount = it
                }

                node.setConcentration(molA, 1.0)
                node.setConcentration(molB, 2.0)
                node.setConcentration(molB, 5.0)

                lastCount shouldBe 2
                emissions shouldBe 3
            }
        }
    }

    context("observeContains") {

        test("observeContains should track presence of molecules") {
            withObservableTestEnvironment {
                val mol = Molecule { "X" }
                val node = spawnNode(0.0, 0.0)

                val seen = mutableListOf<Boolean>()
                node.observeContains(mol).onChange(this) { seen += it }

                seen[0] shouldBe false

                node.setConcentration(mol, 1.0)
                node.removeConcentration(mol)

                seen shouldBe listOf(false, true, false)
            }
        }
    }

    context("observeConcentration") {

        test("observeConcentration should emit Some on set and None on removal") {
            withObservableTestEnvironment {
                val mol = Molecule { "X" }
                val node = spawnNode(0.0, 0.0)

                val seen = mutableListOf<Option<Double>>()
                node.observeConcentration(mol).onChange(this) { seen += it }

                seen[0] shouldBe arrow.core.none()

                node.setConcentration(mol, 1.0)
                node.setConcentration(mol, 2.0)
                node.removeConcentration(mol)

                seen[1] shouldBe 1.0.some()
                seen[2] shouldBe 2.0.some()
                seen[3] shouldBe arrow.core.none()
            }
        }
    }

    context("upsertConcentration") {

        test("upsertConcentration should insert when missing and update when present") {
            withObservableTestEnvironment {
                val mol = Molecule { "U" }
                val node = spawnNode(0.0, 0.0)

                val seen = mutableListOf<Option<Double>>()
                node.observeConcentration(mol).onChange(this) { seen += it }

                // Insert
                node.upsertConcentration(mol) { current -> (current ?: 0.0) + 1.0 }
                // Update
                node.upsertConcentration(mol) { current -> (current ?: 0.0) + 1.0 }

                node.removeConcentration(mol)
                node.upsertConcentration(mol) { current -> (current ?: 10.0) }

                seen[0] shouldBe arrow.core.none()
                seen[1] shouldBe 1.0.some()
                seen[2] shouldBe 2.0.some()
                seen[3] shouldBe arrow.core.none()
                seen[4] shouldBe 10.0.some()

                node.getConcentration(mol) shouldBe 10.0
            }
        }
    }

    context("removeConcentration and setConcentration consistency") {

        test("removeConcentration should keep source node and observableContents in sync") {
            withObservableTestEnvironment {
                val mol = Molecule { "R" }
                val node = spawnNode(0.0, 0.0).apply { setConcentration(mol, 5.0) }

                var lastOpt: Option<Double>? = null
                node.observeConcentration(mol).onChange(this) { lastOpt = it }

                node.removeConcentration(mol)

                lastOpt shouldBe arrow.core.none()
                (mol in node) shouldBe false
                node.observableContents.asMap().keys shouldNotContain mol
            }
        }

        test("setConcentration should update both source and observableContents") {
            withObservableTestEnvironment {
                val mol = Molecule { "S" }
                val node = spawnNode(0.0, 0.0)

                val seen = mutableListOf<Option<Double>>()
                node.observeConcentration(mol).onChange(this) { seen += it }

                node.setConcentration(mol, 1.0)
                node.setConcentration(mol, 2.0)

                seen[0] shouldBe arrow.core.none()
                seen[1] shouldBe 1.0.some()
                seen[2] shouldBe 2.0.some()

                node.getConcentration(mol) shouldBe 2.0
                node.observableContents.asMap()[mol] shouldBe 2.0
            }
        }
    }

    context("dispose") {

        test("dispose should stop emitting from observableMoleculeCount and observableContents") {
            withObservableTestEnvironment {
                val mol = Molecule { "D" }
                val node = spawnNode(0.0, 0.0)

                var countEmissions = 0
                var contentsEmissions = 0

                node.observableMoleculeCount.onChange(this) { countEmissions++ }
                node.observableContents.onChange(this) { contentsEmissions++ }

                countEmissions shouldBe 1
                contentsEmissions shouldBe 1

                node.dispose()

                // wont notify observers
                node.setConcentration(mol, 1.0)
                node.setConcentration(mol, 2.0)
                node.removeConcentration(mol)

                countEmissions shouldBe 1
                contentsEmissions shouldBe 1
            }
        }
    }

    context("cloneNode") {

        test("cloneNode should produce a new ObservableNode wrapping a cloned source") {
            withObservableTestEnvironment {
                val mol = Molecule { "C" }
                val node = spawnNode(0.0, 0.0).apply { setConcentration(mol, 1.0) }

                val cloned = node.cloneNode(Time.ZERO).asObservableNode()

                cloned.getConcentration(mol) shouldBe node.getConcentration(mol)

                cloned.setConcentration(mol, 5.0)

                node.getConcentration(mol) shouldBe 1.0
                cloned.getConcentration(mol) shouldBe 5.0
            }
        }
    }
})
