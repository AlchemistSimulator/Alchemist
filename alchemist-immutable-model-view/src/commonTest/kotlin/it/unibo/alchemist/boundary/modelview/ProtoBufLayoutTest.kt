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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Pins the ProtoBuf bytes of each type. Fields are numbered in declaration order, so reordering, inserting or
 * removing one breaks snapshots written before: if this fails after such a change, number the fields with
 * `ProtoNumber` from then on, matching the old order.
 * Bytes: a tag `(field shl 3) or wireType` (0 varint, 1 double, 2 bytes or message), then the value;
 * fields at their default are not written; enums are their ordinal.
 */
class ProtoBufLayoutTest {
    @Test
    fun `a simulation snapshot keeps its field order`() = assertLayout(
        SimulationSnapshot(
            time = 0.5,
            step = 3,
            status = SimulationStatus.PAUSED,
            dimensions = 3,
            nodes = listOf(NodeSnapshot(id = 4, coordinates = emptyList())),
            links = LinksSnapshot(emptyList(), total = 1),
        ),
        "09 000000000000e03f 10 03 18 02 20 03 2a 02 0804 32 02 1001",
    )

    @Test
    fun `a node keeps its field order`() = assertLayout(
        NodeSnapshot(id = 4, coordinates = listOf(1.0), details = NodeDetails(properties = listOf("p"))),
        "08 04 11 000000000000f03f 22 03 1a0170",
    )

    @Test
    fun `node details keep their field order`() = assertLayout(
        NodeDetails(reactions = listOf(ReactionSnapshot("t", 0.0, executable = false)), properties = listOf("p")),
        "12 0e 0a0174 110000000000000000 1800 1a 01 70",
    )

    @Test
    fun `a reaction keeps its field order`() = assertLayout(
        ReactionSnapshot(
            type = "t",
            nextOccurrence = 0.0,
            executable = true,
            meanDelay = 0.5,
            timeDistribution = "d",
            conditions = listOf("c"),
            actions = listOf("a"),
            text = "x",
        ),
        "0a 01 74 11 0000000000000000 18 01 21 000000000000e03f 2a 01 64 32 01 63 3a 01 61 42 01 78",
    )

    @Test
    fun `links keep their field order`() = assertLayout(
        LinksSnapshot(listOf(LinkSnapshot(1, 2)), total = 5),
        "0a 04 0801 1002 10 05",
    )

    @Test
    fun `values keep their field order`() {
        assertLayout(ValueSnapshot.Numeric(0.5, "x"), "09 000000000000e03f 12 01 78")
        assertLayout(ValueSnapshot.Flag(true), "08 01")
        assertLayout(ValueSnapshot.Text("x"), "0a 01 78")
        assertLayout(ValueSnapshot.Items(emptyList(), size = 2, unique = true), "10 02 18 01")
        assertLayout(ValueSnapshot.Entries(emptyList(), size = 3), "10 03")
        assertLayout(ValueLimits(items = 1, depth = 2, textLength = 3), "08 01 10 02 18 03")
    }

    private companion object {
        inline fun <reified T> assertLayout(value: T, expectedHex: String) =
            assertLayout(serializer<T>(), value, expectedHex)

        fun <T> assertLayout(serializer: KSerializer<T>, value: T, expectedHex: String) {
            val bytes = SnapshotFormats.protoBuf.encodeToByteArray(serializer, value)
            assertEquals(
                expectedHex.replace(" ", ""),
                bytes.joinToString("") {
                    it.toUByte().toString(16).padStart(2, '0')
                },
            )
        }
    }
}
