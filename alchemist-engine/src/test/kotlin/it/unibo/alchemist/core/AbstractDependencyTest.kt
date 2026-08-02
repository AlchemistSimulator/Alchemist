/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.core.util.DependencyUtils.withIncarnation
import it.unibo.alchemist.core.util.DependencyUtils.withRandom
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import org.apache.commons.math3.random.MersenneTwister

abstract class AbstractDependencyTest : StringSpec() {

    abstract fun Reaction<Double>.assertDependencies(vararg dependencies: Reaction<Double>)

    open fun beforeTest(environment: Environment<Double, *>) {}

    init {
        "local reactions on separate nodes should be isolated" {
            val expectedDependencies = linkedMapOf(
                "[a]-->[b]" to listOf("[a]-->[c]", "[b]-->[c]"),
                "[a]-->[c]" to listOf("[a]-->[b]", "[c]-->[b]"),
                "[b]-->[c]" to listOf("[c]-->[b]"),
                "[c]-->[b]" to listOf("[b]-->[c]"),
            )
            expectedDependencies.forEach { (sourceConfiguration, dependencyConfigurations) ->
                withRandom(MersenneTwister(10)) {
                    withIncarnation(BiochemistryIncarnation()) {
                        val reactions: MutableMap<Int, Map<String, Reaction<Double>>> = mutableMapOf()
                        val environment =
                            environment {
                                fun Node<Double>.configureNode(): Map<String, Reaction<Double>> =
                                    expectedDependencies.keys.associateWith { reaction(it) }

                                listOf(0 to 0, 0.5 to 0).forEach { (x, y) ->
                                    node(x, y) {
                                        reactions += id to configureNode()
                                        listOf("a", "b", "c").forEach {
                                            setConcentration(incarnation.createMolecule(it), 1.0)
                                        }
                                    }
                                }
                            }

                        beforeTest(environment)

                        fun String.inNode(id: Int): Reaction<Double> = reactions.getValue(id).getValue(this)

                        (0..1).forEach { id ->
                            sourceConfiguration.inNode(id).assertDependencies(
                                *dependencyConfigurations.map { it.inNode(id) }.toTypedArray(),
                            )
                        }
                    }
                }
            }
        }
    }
}
