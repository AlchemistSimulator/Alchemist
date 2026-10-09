/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.conditions

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.apache.commons.math3.random.MersenneTwister

class ConcentrationChangedTest {
    private val molecule = SimpleMolecule("tracked")

    /** A condition on a node whose [molecule] is observed as [initial], and read as [initialRead] when absent. */
    private fun conditionOn(initial: Option<Double?>, initialRead: Double?) = observe(initial).let { observed ->
        val node = mockk<Node<Double?>>(relaxed = true) {
            every { getConcentration(molecule) } returns initialRead
            every { observeConcentration(molecule) } returns observed
        }
        observed to ConcentrationChanged(GenericReaction(node, ExponentialTime(1.0, MersenneTwister(1))), molecule)
    }

    @Test
    fun `an absent molecule read as the default concentration is not a change`() {
        val (_, condition) = conditionOn(initial = none(), initialRead = 0.0)
        assertFalse(condition.isValid.current)
    }

    @Test
    fun `removing a molecule with a null concentration is a change`() {
        val (concentration, condition) = conditionOn(initial = null.some(), initialRead = null)
        assertFalse(condition.isValid.current)
        concentration.current = none()
        assertTrue(condition.isValid.current)
    }
}
