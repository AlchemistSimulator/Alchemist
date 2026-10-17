/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import it.unibo.alchemist.model.observables.ObservableMutableList
import it.unibo.alchemist.model.observables.ObservableMutableList.Companion.toObservableList
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableMutableListSizeTest {
    private fun assertSize(expected: Int, list: ObservableMutableList<Int>) {
        assertEquals(expected, list.size.current)
        var replayed = -1
        list.size.subscribe { replayed = it }
        assertEquals(expected, replayed)
    }

    @Test
    fun `a list created from items starts with their size`() = assertSize(3, ObservableMutableList(1, 2, 3))

    @Test
    fun `a list converted from a list starts with its size`() = assertSize(2, listOf(1, 2).toObservableList())

    @Test
    fun `an empty list starts with size zero`() = assertSize(0, ObservableMutableList())
}
