/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

/** How snapshots are written to be sent or stored. */
object SnapshotFormats {
    /** JSON, keeping infinite and NaN values, which simulations often hold. */
    val json: Json = Json { allowSpecialFloatingPointValues = true }

    /**
     * ProtoBuf: more compact than JSON.
     * Fields are numbered in declaration order, so the bytes are not stable across versions of Alchemist:
     * both ends must use the same one.
     */
    // The ProtoBuf format is experimental: the opt-in stays here, as it is exposed as a plain BinaryFormat.
    @OptIn(ExperimentalSerializationApi::class)
    val protoBuf: BinaryFormat = ProtoBuf
}
