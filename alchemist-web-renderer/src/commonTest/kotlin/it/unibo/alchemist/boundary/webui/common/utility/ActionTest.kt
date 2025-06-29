/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.utility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionTest {

    @Test
    fun `SimulationAction are PLAY and PAUSE`() {
        val actions = Action.entries.toList()
        assertEquals(2, actions.size, "There should be exactly two simulation actions")
        assertTrue(
            actions.containsAll(listOf(Action.PLAY, Action.PAUSE)),
            "Expected actions to contain PLAY and PAUSE",
        )
    }
}
