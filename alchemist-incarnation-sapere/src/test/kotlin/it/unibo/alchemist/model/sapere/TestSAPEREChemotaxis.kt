/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.actions.SAPEREChemotaxis
import it.unibo.alchemist.model.sapere.nodes.LsaNode

class TestSAPEREChemotaxis :
    StringSpec(
        {
            "SAPEREChemotaxis constructor should throw IllegalArgumentException for negative idPosition" {
                val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
                val environment = Continuous2DEnvironment<List<ILsaMolecule>>(incarnation)
                val node = LsaNode(environment)
                val response = incarnation.createMolecule("response")
                val gradient = incarnation.createMolecule("gradient, Dest")
                val invalidPosition = -1
                val exception = shouldThrow<IllegalArgumentException> {
                    SAPEREChemotaxis(environment, node, response, gradient, invalidPosition)
                }
                exception.message shouldContain "idPosition"
                exception.message shouldContain invalidPosition.toString()
            }
        },
    )
