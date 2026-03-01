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
            withRandom(MersenneTwister(10)) {
                withIncarnation(BiochemistryIncarnation()) {
                    val reactions: MutableMap<Int, Map<String, Reaction<Double>>> = mutableMapOf()
                    val environment =
                        environment {
                            fun Node<Double>.configureNode(): Map<String, Reaction<Double>> = listOf(
                                "[a]-->[b]",
                                "[a]-->[c]",
                                "[b]-->[c]",
                                "[c]-->[b]",
                            ).associateWith { reaction(it) }

                            node(0, 0) {
                                val map = configureNode()
                                reactions += id to map
                                setConcentration(incarnation.createMolecule("a"), 1.0)
                                setConcentration(incarnation.createMolecule("b"), 1.0)
                                setConcentration(incarnation.createMolecule("c"), 1.0)
                            }

                            node(0.5, 0) {
                                val map = configureNode()
                                reactions += id to map
                                setConcentration(incarnation.createMolecule("a"), 1.0)
                                setConcentration(incarnation.createMolecule("b"), 1.0)
                                setConcentration(incarnation.createMolecule("c"), 1.0)
                            }
                        }

                    beforeTest(environment)

                    fun String.inNode(id: Int): Reaction<Double> = reactions.getValue(id).getValue(this)

                    (0..1).forEach { id ->
                        "[a]-->[b]".inNode(id).assertDependencies(
                            "[a]-->[c]".inNode(id),
                            "[b]-->[c]".inNode(id),
                        )

                        "[a]-->[c]".inNode(id).assertDependencies(
                            "[a]-->[b]".inNode(id),
                            "[c]-->[b]".inNode(id),
                        )

                        "[b]-->[c]".inNode(id).assertDependencies(
                            "[c]-->[b]".inNode(id),
                        )

                        "[c]-->[b]".inNode(id).assertDependencies(
                            "[b]-->[c]".inNode(id),
                        )
                    }
                }
            }
        }
    }
}
