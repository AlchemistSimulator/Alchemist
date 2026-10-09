/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import arrow.core.Option
import arrow.core.some
import it.unibo.alchemist.model.observables.ObservableMutableMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObservableMutableMapNullValueTest {
    private val map = ObservableMutableMap<String, Int?>()
    private val mapUpdates = mutableListOf<Map<String, Int?>>()
    private val keyUpdates = mutableListOf<Option<Int?>>()

    init {
        map.subscribe(invokeOnSubscription = false) { mapUpdates += it }
        map["a"].subscribe(invokeOnSubscription = false) { keyUpdates += it }
    }

    private fun assertNullValueInserted() {
        assertTrue("a" in map.current)
        assertEquals(listOf(mapOf<String, Int?>("a" to null)), mapUpdates)
        assertEquals(listOf<Option<Int?>>(null.some()), keyUpdates)
    }

    @Test
    fun `putting null on an absent key inserts it and notifies once`() {
        map.put("a", null)
        assertNullValueInserted()
        map.put("a", null)
        assertNullValueInserted()
    }

    @Test
    fun `replacing the content with a null value on an absent key inserts it and notifies once`() {
        map.clearAndPutAll(mapOf("a" to null))
        assertNullValueInserted()
        map.clearAndPutAll(mapOf("a" to null))
        assertNullValueInserted()
    }
}
