/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.util

import kotlin.jvm.JvmInline

/**
 * Two [Int]s stored in one [Long], for use as compact keys.
 *
 * @property packed both numbers, [high] in the upper half.
 */
@JvmInline
internal value class DoubleInt(val packed: Long) {
    /** The first number of the pair. */
    val high: Int get() = (packed ushr Int.SIZE_BITS).toInt()

    /** The second number of the pair. */
    val low: Int get() = packed.toInt()

    /** Packs [high] and [low] together. */
    constructor(high: Int, low: Int) : this((high.toLong() shl Int.SIZE_BITS) or low.toUInt().toLong())
}
