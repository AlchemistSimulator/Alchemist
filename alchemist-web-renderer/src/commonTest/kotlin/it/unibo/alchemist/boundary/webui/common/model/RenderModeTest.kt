/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RenderModeTest {

    @Test
    fun `RenderMode are just CLIENT, SERVER and AUTO`() {
        val renderModes = RenderMode.entries.toList()
        assertEquals(3, renderModes.size, "There should be exactly three render modes")
        assertTrue(
            renderModes.containsAll(listOf(RenderMode.CLIENT, RenderMode.SERVER, RenderMode.AUTO)),
            "Expected render modes to contain CLIENT, SERVER, and AUTO",
        )
    }
}
