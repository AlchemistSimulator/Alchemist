/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.nodes

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import kotlin.test.Test
import kotlin.test.assertEquals

class LsaNodeContentsTest {
    private val node = LsaNode(Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>()))
    private val molecule = LsaMolecule("grad, 1")

    private fun count(contents: Map<Molecule, List<ILsaMolecule>>): Map<Molecule, Any?> =
        contents.mapValues { (_, value) -> value.single().getArg(0).rootNodeData }

    @Test
    fun `subscribers of the observable contents receive the molecule counts`() {
        val received = mutableListOf<Map<Molecule, Any?>>()
        node.observableContents.onChange(this, invokeOnRegistration = false) { received += count(it) }
        node.setConcentration(molecule)
        node.setConcentration(molecule)
        node.removeConcentration(molecule)
        node.removeConcentration(molecule)
        assertEquals(
            listOf(mapOf(molecule to 1.0), mapOf(molecule to 2.0), mapOf(molecule to 1.0), emptyMap()),
            received,
        )
    }
}
