/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.actions.SAPEREChemotaxis
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

class TestSAPEREChemotaxis {

    private val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
    private val environment = Continuous2DEnvironment<List<ILsaMolecule>>(incarnation)
    private val node = LsaNode(environment)
    private val response = incarnation.createMolecule("response")
    private val gradient = incarnation.createMolecule("gradient, Dest")

    @Test
    fun `negative idPosition throws IllegalArgumentException with descriptive message`() {
        val invalidPosition = -1
        val exception = assertFailsWith<IllegalArgumentException> {
            SAPEREChemotaxis(environment, node, response, gradient, invalidPosition)
        }
        assertContains(exception.message.orEmpty(), "idPosition")
        assertContains(exception.message.orEmpty(), invalidPosition.toString())
    }
}
