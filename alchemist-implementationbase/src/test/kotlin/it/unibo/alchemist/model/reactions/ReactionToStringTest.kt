/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.reactions

import io.mockk.mockk
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.timedistributions.DiracComb
import kotlin.test.Test
import kotlin.test.assertTrue

class ReactionToStringTest {
    @Test
    fun `a reaction describes itself with its own class name`() {
        val description = GenericReaction(mockk<Node<Any>>(relaxed = true), DiracComb(1.0)).toString()
        assertTrue(description.startsWith("GenericReaction@"), description)
    }
}
