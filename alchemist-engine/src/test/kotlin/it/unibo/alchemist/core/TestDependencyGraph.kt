/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction

class TestDependencyGraph : AbstractDependencyTest() {

    private lateinit var graph: DependencyGraph<Double>

    override fun beforeTest(environment: Environment<Double, *>) {
        graph = JGraphTDependencyGraph(environment)
        environment.nodes.flatMap { it.reactions }.forEach { graph.createDependencies(it) }
    }

    override fun Reaction<Double>.assertDependencies(vararg dependencies: Reaction<Double>) {
        graph.outboundDependencies(this).toList() shouldContainExactlyInAnyOrder dependencies.toList()
    }
}
