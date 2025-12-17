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
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveReactionAdapter
import it.unibo.alchemist.rx.model.adapters.reaction.asReactive
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withObservableTestEnvironment
import org.apache.commons.math3.random.MersenneTwister

/**
 * Similar properties testing as in `TestDependencyGraph` defined in engine's tests.
 */
class ReactiveDependencyTest : FunSpec({

    test("local reactions on separate nodes should be isolated and correctly wired") {
        withObservableTestEnvironment {
            val incarnation = TestEnvironmentFactory.testIncarnation
            val rng = MersenneTwister(10)

            val n0 = spawnNode(0.0, 0.0)
            val n1 = spawnNode(0.5, 0.0)

            val a = Molecule { "a" }
            val b = Molecule { "b" }
            val c = Molecule { "c" }

            listOf(n0, n1).forEach { node ->
                node.setConcentration(a, 10.0)
                node.setConcentration(b, 10.0)
                node.setConcentration(c, 10.0)
            }

            fun createReactions(node: ObservableNode<Double>): Map<String, ReactiveReactionAdapter<Double>> {
                val definitions = listOf(
                    "[a]-->[b]",
                    "[a]-->[c]",
                    "[b]-->[c]",
                    "[c]-->[b]",
                )

                return definitions.associateWith { def ->
                    val reaction = incarnation.createReaction(
                        rng,
                        this,
                        node,
                        ExponentialTime(1.0, rng),
                        def,
                    )
                    node.addReaction(reaction)
                    reaction.asReactive(this)
                }
            }

            val r0 = createReactions(n0)
            val r1 = createReactions(n1)

            val rescheduled = mutableSetOf<ReactiveReactionAdapter<Double>>()
            (r0.values + r1.values).forEach { r ->
                r.rescheduleRequest.onChange(this) { rescheduled.add(r) }
            }
            rescheduled.clear() // clear initial callbacks calls

            val source = r0["[a]-->[b]"] ?: error("Reaction not found")
            source.execute()

            rescheduled shouldContain r0["[a]-->[c]"]
            rescheduled shouldContain r0["[b]-->[c]"]
            rescheduled shouldNotContain r0["[c]-->[b]"]

            r1.values.forEach { reaction -> rescheduled shouldNotContain reaction }
        }
    }
})
