/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.nodes

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import kotlin.test.Test
import kotlin.test.assertEquals

class LsaNodeTemplateMatchingTest {
    private val node = LsaNode(Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>()))
    private val named = LsaMolecule("foo, 2")
    private val numeric = LsaMolecule("1, 2")

    init {
        node.addLsa(named)
        node.addLsa(numeric)
        node.addLsa(LsaMolecule("foo, 3"))
    }

    private fun matchesOf(template: String): List<ILsaMolecule> = node.matching(LsaMolecule(template))

    @Test
    fun `a template with a variable first argument matches any first argument`() {
        assertEquals(setOf(named, numeric), matchesOf("A, 2").toSet())
    }

    @Test
    fun `a template with a numeric first argument matches the instances with that number`() {
        assertEquals(listOf(numeric), matchesOf("1, X"))
    }

    @Test
    fun `observing a template with a variable first argument follows later additions`() {
        val contained = mutableListOf<Boolean>()
        node.containsMolecule(LsaMolecule("A, 4")).subscribe { contained += it }
        node.addLsa(LsaMolecule("bar, 4"))
        assertEquals(listOf(false, true), contained)
    }

    @Test
    fun `a template with a variable first argument removes a matching instance`() {
        node.removeOneMatchedLsa(LsaMolecule("A, 3"))
        assertEquals(setOf(named, numeric), node.lsaSpace.toSet())
        assertEquals(setOf(named, numeric), node.contents.current.keys)
    }
}
