/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.deployments

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class TestFromGPSTrace {
    @Test
    fun `inferred node count matches trace count`() {
        assertEquals(3, FromGPSTrace(TRACE_DIRECTORY, ALIGNMENT).stream().count())
    }

    @Test
    fun `non-cyclic deployments cannot request more nodes than traces`() {
        assertFailsWith<IllegalArgumentException> {
            FromGPSTrace(4, TRACE_DIRECTORY, false, ALIGNMENT)
        }
    }

    @Test
    fun `cyclic deployments can request more nodes than traces`() {
        assertEquals(4, FromGPSTrace(4, TRACE_DIRECTORY, true, ALIGNMENT).stream().count())
    }

    private companion object {
        private const val TRACE_DIRECTORY = "trace/ok/sub1/"
        private const val ALIGNMENT = "AlignToSimulationTime"
    }
}
