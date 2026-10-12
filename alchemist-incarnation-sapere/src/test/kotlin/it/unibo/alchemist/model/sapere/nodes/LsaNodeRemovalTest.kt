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
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import kotlin.test.Test
import kotlin.test.assertEquals

class LsaNodeRemovalTest {
    private val node = LsaNode(Continuous2DEnvironment(SAPEREIncarnation()))

    @Test
    fun `removing through a matching template removes the matched instance from the LSA space`() {
        val kept = LsaMolecule("other, 2")
        node.setConcentration(LsaMolecule("grad, 1"))
        node.setConcentration(kept)
        node.removeConcentration(LsaMolecule("grad, X"))
        assertEquals(listOf(kept), node.lsaSpace)
        assertEquals(listOf(kept), node.observeLsaSpace().current)
        assertEquals(1, node.moleculeCount)
    }
}
