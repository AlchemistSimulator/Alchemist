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
import arrow.core.getOrElse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.rx.dsl.ReactiveConditionDSL.condition
import it.unibo.alchemist.rx.model.ReactiveCondition.Companion.validityToPropensity
import it.unibo.alchemist.rx.model.adapters.ObservableNeighborhood
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.combineLatest
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withObservableTestEnvironment

class ReactiveConditionTest : FunSpec({
    test("a simple condition could be constructed through the dsl") {
        val obs = observe(10.0)
        val condition = condition<Double> {
            validity {
                val value by depending(obs)
                value > 10.0
            }
            propensity { if (it) 1.0 else 0.0 }
        }

        var shouldBeTrue = false
        condition.isValid.onChange(this) {
            it shouldBe shouldBeTrue
        }
        condition.propensityContribution.onChange(this) {
            if (shouldBeTrue) it shouldBe 1.0 else it shouldBe 0.0
        }

        shouldBeTrue = true
        obs.update { it + 10.0 }
    }

    context("condition tests in environment") {
        test("condition should be able to track target molecule in node") {
            withObservableTestEnvironment {
                val node = spawnNode(0.0, 0.0)
                val target = Molecule { "A" }
                val condition = condition<Double> {
                    validity {
                        val concentration by depending(node.observeConcentration(target))
                        concentration.getOrElse { -1.0 } >= 10.0
                    }
                    propensity { if (it) 1.0 else 0.0 }
                }

                val changes = mutableListOf<Pair<Boolean, Double>>()

                condition.validityToPropensity().onChange(this, changes::add)

                getNodeByID(node.id).setConcentration(target, 0.0)
                getNodeByID(node.id).setConcentration(target, 11.0)

                changes[0] shouldBe (false to 0.0)
                changes[1] shouldBe (true to 1.0)
            }
        }

        test("condition should be able to track target node position") {
            withObservableTestEnvironment {
                val node = spawnNode(0.0, 0.0)

                val changes = mutableListOf<Pair<Boolean, Double>>()
                val isCenterCondition = condition<Double> {
                    validity {
                        val position by depending(observeNodePosition(node))
                        position.getOrNull() == makePosition(0.0, 0.0)
                    }
                    propensity { if (it) 1.0 else 0.0 }
                }

                isCenterCondition.validityToPropensity().onChange(this, changes::add)

                moveNodeToPosition(node, makePosition(100.0, 100.0))
                moveNodeToPosition(node, makePosition(0.0, 0.0))

                changes[0] shouldBe (true to 1.0)
                changes[1] shouldBe (false to 0.0)
                changes[2] shouldBe (true to 1.0)
            }
        }

        test("condition should be able to track node's neighbourhood") {
            withObservableTestEnvironment {
                val node = spawnNode(0.0, 0.0)
                val target = Molecule { "A" }
                val someNeigbhorHasTargetMoleculeCondition = condition<Double> {
                    validity {
                        val nodeN: Option<ObservableNeighborhood<Double>> by depending(observeNeighborhood(node))
                        if (nodeN.isNone()) return@validity false

                        val hasNeighborhoodTarget by depending(
                            nodeN.getOrNull()!!.combineLatest({ it.observeContains(target) }) { bools ->
                                bools.any { it }
                            },
                        )
                        hasNeighborhoodTarget
                    }
                    propensity { if (it) 1.0 else 0.0 }
                }

                val seen = mutableListOf<Pair<Boolean, Double>>()
                someNeigbhorHasTargetMoleculeCondition.validityToPropensity().onChange(this, seen::add)

                val neighbor = spawnNode(1.0, 0.0).also {
                    getNodeByID(it.id).setConcentration(target, 1.0)
                }

                seen[0] shouldBe (false to 0.0)
                seen[1] shouldBe (true to 1.0)

                removeNode(neighbor)

                seen[2] shouldBe (false to 0.0)
            }
        }

        test("condition should expose its dependencies") {
            val obs1 = observe(1)
            val obs2 = observe(2)
            val condition = condition<Double> {
                validity {
                    val v1 by depending(obs1)
                    v1 > 0
                }
                propensity {
                    val v2 by depending(obs2)
                    if (it) v2.toDouble() else 0.0
                }
            }

            (obs1 in condition.observableInboundDependencies) shouldBe true
            (obs2 in condition.observableInboundDependencies) shouldBe true
        }
    }
})
