/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatIdRanges
import kotlin.test.Test
import kotlin.test.assertEquals

class IdRangesTest {
    @Test
    fun `runs of three or more ids become ranges`() {
        assertEquals("[0..2], 4, 6, 7, [9..11]", listOf(0, 1, 2, 4, 6, 7, 9, 10, 11).formatIdRanges())
    }

    @Test
    fun `ids are sorted and duplicates dropped`() {
        assertEquals("[1..3], 5", listOf(5, 3, 1, 2, 3).formatIdRanges())
    }

    @Test
    fun `a single id stays as it is`() {
        assertEquals("7", listOf(7).formatIdRanges())
    }

    @Test
    fun `no ids give an empty text`() {
        assertEquals("", emptyList<Int>().formatIdRanges())
    }
}
