/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.util

/**
 * Turns values into text for the UI.
 */
internal object DisplayValues {
    private const val DISPLAYED_DECIMALS = 3
    private const val THOUSAND = 1_000L
    private const val MILLION = 1_000_000L
    private const val SHORTEST_RANGE = 3

    /**
     * Decimals get three digits, `null` becomes empty, anything else uses [toString].
     */
    fun Any?.toDisplayString(): String = when (this) {
        null -> ""
        is Double -> formatFixed(DISPLAYED_DECIMALS)
        is Float -> toDouble().formatFixed(DISPLAYED_DECIMALS)
        else -> toString()
    }

    /**
     * Shortens this number with K and M, keeping one decimal if needed: 1200 becomes 1.2K, 10000 becomes 10K.
     */
    fun Long.formatCompact(): String = when {
        this >= MILLION -> compact(MILLION, "M")
        this >= THOUSAND -> compact(THOUSAND, "K")
        else -> toString()
    }

    /**
     * Shortens this number as [Long.formatCompact] if it is a whole number, or leaves it as it is otherwise.
     */
    fun Double.formatCompact(): String =
        if (this == kotlin.math.floor(this) && this < Long.MAX_VALUE) toLong().formatCompact() else toString()

    /**
     * Formats this number with exactly [decimals] digits after the point.
     */
    fun Double.formatFixed(decimals: Int): String {
        val safeDecimals = decimals.coerceAtLeast(0)
        val factor = (1..safeDecimals).fold(1.0) { acc, _ -> acc * 10.0 }
        val rounded = kotlin.math.round(this * factor) / factor
        val raw = rounded.toString()
        return when {
            safeDecimals == 0 -> raw.substringBefore('.')
            '.' !in raw -> raw + "." + "0".repeat(safeDecimals)
            else -> {
                val fractional = raw.substringAfter('.')
                raw + "0".repeat((safeDecimals - fractional.length).coerceAtLeast(0))
            }
        }
    }

    /**
     * Lists these ids in order, writing runs of three or more consecutive ids as ranges:
     * 0, 1, 2, 4, 6, 7, 9, 10, 11 becomes `[0..2], 4, 6, 7, [9..11]`.
     */
    fun Iterable<Int>.formatIdRanges(): String = distinct().sorted()
        .fold(mutableListOf<IntRange>()) { runs, id ->
            val last = runs.lastOrNull()
            if (last != null && last.last + 1 == id) runs[runs.lastIndex] = last.first..id else runs += id..id
            runs
        }
        .joinToString(", ") { run ->
            if (run.last - run.first + 1 >= SHORTEST_RANGE) "[${run.first}..${run.last}]" else run.joinToString(", ")
        }

    /** The name of the type of this value, or `null` for a missing one. */
    fun Any?.typeName(): String = this?.let { it::class.simpleName ?: "unnamed type" } ?: "null"

    private fun Long.compact(unit: Long, suffix: String): String =
        (if (this % unit == 0L) (this / unit).toString() else (toDouble() / unit).formatFixed(1)) + suffix
}
