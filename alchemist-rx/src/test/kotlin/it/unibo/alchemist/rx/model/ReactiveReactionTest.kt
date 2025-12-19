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
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.rx.dsl.ReactiveConditionDSL.condition
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withObservableTestEnvironment

class ReactiveReactionTest : FunSpec({

    class TestReactiveReaction<T>(
        node: ObservableNode<T>,
        timeDistribution: TimeDistribution<T> = DiracComb<T>(1.0),
    ) : AbstractReactiveReaction<T>(node, timeDistribution) {

        override fun cloneOnNewNode(node: ObservableNode<T>, currentTime: Time): ReactiveReaction<T> =
            TestReactiveReaction(node, timeDistribution.cloneOnNewNode(node, currentTime))

        @Suppress("EmptyFunctionBlock")
        override fun updateInternalStatus(
            currentTime: Time,
            hasBeenExecuted: Boolean,
            environment: ObservableEnvironment<T, *>,
        ) {}
    }

    test("reaction should reschedule when condition dependencies change") {
        withObservableTestEnvironment {
            val node = spawnNode(0.0, 0.0)
            val reaction = TestReactiveReaction(node)

            val dependency = observe(10)
            val condition = condition<Double> {
                validity {
                    val v by depending(dependency)
                    v > 5
                }
                propensity { 1.0 }
            }

            reaction.conditions = listOf(condition)

            var rescheduleCount = 0
            reaction.rescheduleRequest.onChange(this) { rescheduleCount++ }

            val baseline = rescheduleCount

            dependency.current = 4
            rescheduleCount shouldBe baseline + 1

            dependency.current = 6
            rescheduleCount shouldBe baseline + 2
        }
    }

    test("reaction canExecute should reflect conditions validity") {
        withObservableTestEnvironment {
            val node = spawnNode(0.0, 0.0)
            val reaction = TestReactiveReaction(node)

            val validObs = observe(true)
            val condition = condition<Double> {
                validity {
                    val v by depending(validObs)
                    v
                }
                propensity { 1.0 }
            }

            reaction.conditions = listOf(condition)

            reaction.canExecute() shouldBe true

            validObs.current = false
            condition.isValid.current shouldBe false
            reaction.canExecute() shouldBe false
        }
    }

    test("disposing a reaction should remove all of its subscriptions") {
        withObservableTestEnvironment {
            val node = spawnNode(0.0, 0.0)
            val cond = condition<Double> {
                validity {
                    true
                }
                propensity { 1.0 }
            }

            val baseline = cond.isValid.observers.size

            val reaction = TestReactiveReaction(node).apply {
                conditions = listOf(cond)
            }

            cond.isValid.observers.filterIsInstance<Pair<Any, Any>>().first().toList() shouldContain reaction
            cond.isValid.observers.size shouldBe baseline + 1
            reaction.dispose()
            cond.isValid.observers.size shouldBe baseline
        }
    }
})
