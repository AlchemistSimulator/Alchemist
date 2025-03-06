/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.protelis

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.molecules.SimpleMolecule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kaikikm.threadresloader.ResourceLoader

/** Tests node cloning. */
internal class TestNodeCloning<P : Position<P>> {
    private fun Environment<Any, *>.makeNode(x: Double, y: Double, enabled: Boolean, source: Boolean) {
        getNodeByID(0).cloneNode(Time.ZERO).apply {
            setConcentration(SOURCEMOL, source)
            setConcentration(ENABLEDMOL, enabled)
            addNode(this, makePosition(x, y))
        }
    }

    /** Tests that gradient values are consistent and stable. */
    @Test
    fun test() {
        val simulation =
            LoadAlchemist
                .from(
                    ResourceLoader.getResource("gradient.yml"),
                ).getWith<Any, P>(emptyMap<String, Nothing>())
        val environment = simulation.environment
        simulation.schedule {
            environment.getNodeByID(0).apply {
                setConcentration(SOURCEMOL, false)
                setConcentration(ENABLEDMOL, true)
                environment.moveNodeToPosition(this, environment.makePosition(-30.72191619873047, -9.75))
            }
            environment.makeNode(-34.62321853637695, -6.039149761199951, true, false)
            environment.makeNode(-33.585994720458987, -1.3899999856948853, true, true)
            environment.makeNode(-26.3700008392334, -9.899999618530274, false, false)
        }
        val nid: (Int) -> Node<Any> = { environment.getNodeByID(it) }
        val distance: (Int, Int) -> Double = { a, b -> environment.getDistanceBetweenNodes(nid(a), nid(b)) }
        simulation.addOutputMonitor(
            object : OutputMonitor<Any, P> {
                override fun stepDone(
                    currentEnvironment: Environment<Any, P>,
                    reaction: Actionable<Any>?,
                    time: Time,
                    step: Long,
                ) {
                    val expectations =
                        mapOf(
                            nid(2) to 0.0,
                            nid(1) to distance(2, 1),
                            nid(0) to distance(2, 1) + distance(1, 0),
                            nid(3) to distance(2, 1) + distance(1, 0) + distance(0, 3),
                        )
                    if (step ==
                        ENABLE_STEP
                    ) {
                        simulation.schedule { currentEnvironment.getNodeByID(3).setConcentration(ENABLEDMOL, true) }
                    }
                    if (step >
                        ENABLE_CHECKS
                    ) {
                        expectations.forEach { (node, expected) ->
                            assertEquals(expected, node.getConcentration(DATAMOL))
                        }
                    }
                }
            },
        )
        simulation.play()
        simulation.run()
        simulation.error.ifPresent { throw it }
    }

    companion object {
        private val SOURCEMOL = SimpleMolecule("source")
        private val ENABLEDMOL = SimpleMolecule("enabled")
        private val DATAMOL = SimpleMolecule("data")
        private const val ENABLE_STEP = 50L
        private const val ENABLE_CHECKS = ENABLE_STEP + 10
    }
}
