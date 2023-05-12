/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model

import java.io.Serializable
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.Double.Companion.NEGATIVE_INFINITY as MINUS_INFINITY

/**
 * Interface for time representation.
 */
interface Time : Comparable<Time>, Serializable {

    /**
     * Verifies if the [Time] is set at infinite, namely if the event will
     * never happen.
     *
     * @return true if the [Time] is infinite
     */
    val isInfinite: Boolean get() = toDouble().isInfinite()

    /**
     * Allows to multiply this [Time] for a constant.
     *
     * @param other
     * the [Time] to sum to the current [Time]
     *
     * @return the result of the multiplication
     */
    operator fun times(other: Double): Time

    /**
     * Allows to subtract a [Time] to this [Time].
     *
     * @param other
     * the time to subtract from the current [Time]
     *
     * @return the result of the subtraction
     */
    operator fun minus(other: Time): Time

    /**
     * Allows to add a [Time] to this [Time].
     *
     * @param other
     * the time to sum to the current [Time]
     *
     * @return the result of the sum
     */
    operator fun plus(other: Time): Time

    /**
     * Allows to get a double representation of this [Time].
     *
     * @return the double representation of this [Time]
     */
    fun toDouble(): Double

    companion object {
        /**
         * Initial time.
         */
        @JvmField
        val ZERO: Time = object : Time {
            override val isInfinite: Boolean = false

            override fun times(other: Double): Time =
                takeIf { other.isFinite() } ?: error("Cannot multiply zero by $other")

            override fun minus(other: Time) = other.times(-1.0)

            override fun plus(other: Time) = other

            override fun toDouble() = 0.0

            override operator fun compareTo(other: Time) = 0.0.compareTo(other.toDouble())

            override fun equals(other: Any?) = other === this || other is Time && other.toDouble() == 0.0

            override fun hashCode() = 0.0.hashCode()

            override fun toString() = "0"
        }

        /**
         * Indefinitely future time.
         */
        @JvmField
        val INFINITY: Time = object : Time {
            override val isInfinite = true

            override fun times(other: Double) = when {
                other > 0.0 -> this
                other < 0.0 -> NEGATIVE_INFINITY
                else -> error("Cannot multiply $this by 0")
            }

            override fun minus(other: Time) =
                takeUnless { other == this } ?: error("Cannot subtract an infinite time from $this")

            override fun plus(other: Time) =
                takeUnless { other == NEGATIVE_INFINITY } ?: error("Cannot subtract an infinite time from $this")

            override fun toDouble() = POSITIVE_INFINITY

            override operator fun compareTo(other: Time) = POSITIVE_INFINITY.compareTo(other.toDouble())

            override fun equals(other: Any?) = other === this || other is Time && other.toDouble() == POSITIVE_INFINITY

            override fun hashCode() = POSITIVE_INFINITY.hashCode()

            override fun toString() = "∞"
        }

        /**
         * Indefinitely past time.
         */
        @JvmField
        val NEGATIVE_INFINITY: Time = object : Time by INFINITY {
            override fun times(other: Double) = when {
                other > 0.0 -> this
                other < 0.0 -> INFINITY
                else -> error("Cannot multiply $this by 0")
            }

            override fun minus(other: Time) =
                takeUnless { other == this } ?: error("Cannot sum an infinite time to $this")

            override fun plus(other: Time) =
                takeUnless { other == INFINITY } ?: error("Cannot sum an infinite time to $this")

            override fun toDouble() = MINUS_INFINITY

            override operator fun compareTo(other: Time) = MINUS_INFINITY.compareTo(other.toDouble())

            override fun equals(other: Any?) = other === this || other is Time && other.toDouble() == MINUS_INFINITY

            override fun hashCode() = MINUS_INFINITY.hashCode()

            override fun toString() = "-∞"
        }
    }
}
