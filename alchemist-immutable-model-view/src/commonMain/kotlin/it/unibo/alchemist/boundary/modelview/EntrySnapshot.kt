/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import kotlinx.serialization.Serializable

/**
 * An entry of a map value.
 *
 * @property key the key.
 * @property value the value.
 */
@Serializable
data class EntrySnapshot(
    val key: ValueSnapshot,
    val value: ValueSnapshot,
)
