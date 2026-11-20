/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model.pacing

import kotlin.math.floor

/**
 * The simulation speed limit: a [mode] and its [rate], which the [PacingMode.MAX_SPEED] mode ignores.
 */
internal data class Pacing(val mode: PacingMode = PacingMode.MAX_SPEED, val rate: Double = mode.defaultRate) {
    init {
        require(rate > 0 && rate.isFinite()) { "The pacing rate must be a positive number, but was $rate." }
    }

    /** The rate as text, without a trailing ".0". */
    val rateLabel: String = if (rate == floor(rate) && rate < Long.MAX_VALUE) rate.toLong().toString() else "$rate"

    /**
     * The real seconds that [events] events, spanning [elapsedTime] simulated time, should take at this pace.
     */
    fun secondsFor(events: Long, elapsedTime: Double): Double = when (mode) {
        PacingMode.MAX_SPEED -> 0.0
        PacingMode.EVENTS_PER_SECOND -> events / rate
        PacingMode.TIME_PER_SECOND -> elapsedTime / rate
    }

    /** Returns the same mode at the rate typed as [text], or this pace if [text] is not a positive number. */
    fun withRate(text: String): Pacing = parseRate(text)?.let { copy(rate = it) } ?: this

    /** Reading rates from text. */
    companion object {
        /** The rate typed as [text], or null if it is not a positive number. */
        fun parseRate(text: String): Double? = text.toDoubleOrNull()?.takeIf { it > 0 && it.isFinite() }
    }
}
