/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatusSurrogateTest {

    private val listOfValues = listOf(
        StatusSurrogate.INIT,
        StatusSurrogate.READY,
        StatusSurrogate.PAUSED,
        StatusSurrogate.RUNNING,
        StatusSurrogate.TERMINATED,
    )

    @Test
    fun `status surrogates should include exactly INIT, READY, PAUSED, RUNNING and TERMINATED`() {
        val statuses = StatusSurrogate.entries.toList()
        assertEquals(5, statuses.size, "There should be exactly five status surrogates")
        assertTrue(
            statuses.containsAll(listOfValues),
            "Expected status surrogates to contain $listOfValues, but was $statuses",
        )
    }

    @Test
    fun `status surrogates should be serializable and deserializable`() {
        listOfValues.forEach { status ->
            val ser = jsonFormat.encodeToString(status)
            val des: StatusSurrogate = jsonFormat.decodeFromString(ser)
            assertEquals(status, des, "Deserialized value should equal original for status $status")
        }
    }
}
