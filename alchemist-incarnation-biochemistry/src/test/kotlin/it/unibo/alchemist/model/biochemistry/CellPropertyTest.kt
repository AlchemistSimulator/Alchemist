/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import kotlin.test.Test
import kotlin.test.assertEquals
import org.apache.commons.math3.random.MersenneTwister

class CellPropertyTest {
    private val randomGenerator = MersenneTwister(0)
    private val incarnation = BiochemistryIncarnation()
    private val environment = BioRect2DEnvironment(incarnation)
    private val cell = incarnation.createNode(randomGenerator, environment, null).asProperty<Double, CellProperty<*>>()
    private val neighbors = List(2) { incarnation.createNode(randomGenerator, environment, null) }
    private val junction = Junction("A-B", emptyMap(), emptyMap())

    @Test
    fun `observed junction links follow neighbors joining and leaving an existing junction`() {
        var linked = emptySet<Any>()
        cell.observeNeighborLinkWithJunction(junction).subscribe { linked = it }
        neighbors.forEach { cell.addJunction(junction, it) }
        assertEquals(neighbors.toSet(), linked)
        cell.removeJunction(junction, neighbors.first())
        assertEquals(setOf(neighbors.last()), linked)
        cell.removeJunction(junction, neighbors.last())
        assertEquals(emptySet(), linked)
    }
}
