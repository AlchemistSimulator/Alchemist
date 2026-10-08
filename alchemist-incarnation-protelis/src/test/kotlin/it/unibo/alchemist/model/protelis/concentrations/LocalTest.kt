/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis.concentrations

import kotlin.test.Test
import kotlin.test.assertEquals

class LocalTest {
    @Test
    fun `an empty local is printable, comparable, and hashable`() {
        assertEquals("null", Local().toString())
        assertEquals(Local(), Local(null))
        assertEquals(Local().hashCode(), Local(null).hashCode())
    }
}
