/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

class SimulationSnapshotTest {
    private val snapshot = SimulationSnapshot(
        time = 1.5,
        step = 3,
        status = SimulationStatus.PAUSED,
        nodes = listOf(
            NodeSnapshot(
                id = 0,
                coordinates = listOf(0.0, 1.0),
                values = mapOf("distance" to ValueSnapshot.of(Double.POSITIVE_INFINITY)),
                details = NodeDetails(
                    contents = listOf(
                        ContentSnapshot("data", ValueSnapshot.of(mapOf("a" to listOf(1, 2))), "LinkedHashMap"),
                    ),
                    reactions = listOf(
                        ReactionSnapshot(
                            type = "Event",
                            nextOccurrence = Double.POSITIVE_INFINITY,
                            executable = false,
                            meanDelay = 1.0,
                            timeDistribution = "DiracComb[ΔT=1.0]",
                            conditions = listOf("x > 1"),
                            actions = listOf("send"),
                        ),
                    ),
                ),
            ),
            NodeSnapshot(1, listOf(2.0, 3.0), mapOf("source" to ValueSnapshot.of(true))),
        ),
        links = LinksSnapshot(listOf(LinkSnapshot(0, 1)), total = 4),
    )

    @Test
    fun `a snapshot survives JSON, infinite values and nested collections included`() {
        val json = SnapshotFormats.json
        assertEquals(snapshot, json.decodeFromString<SimulationSnapshot>(json.encodeToString(snapshot)))
    }

    @Test
    fun `a snapshot survives ProtoBuf, in fewer bytes than JSON`() {
        val protoBuf = SnapshotFormats.protoBuf
        val bytes = protoBuf.encodeToByteArray(snapshot)
        assertEquals(snapshot, protoBuf.decodeFromByteArray<SimulationSnapshot>(bytes))
        assertTrue(bytes.size < SnapshotFormats.json.encodeToString(snapshot).encodeToByteArray().size)
    }
}
