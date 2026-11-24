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
 * How much of a value goes in a snapshot, so that large values stay cheap.
 *
 * @property items how many items or entries of a collection are kept.
 * @property depth how many levels of nested collections are kept; deeper ones keep only their size.
 * @property textLength how many characters of a text are kept.
 */
@Serializable
data class ValueLimits(
    val items: Int = 100,
    val depth: Int = 4,
    val textLength: Int = 1000,
) {
    init {
        require(items >= 0 && depth >= 0 && textLength >= 0) { "Value limits cannot be negative: $this" }
    }
}
