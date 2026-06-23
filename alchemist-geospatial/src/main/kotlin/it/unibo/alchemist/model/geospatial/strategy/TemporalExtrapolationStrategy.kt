/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

/**
 * Strategy for **temporal extrapolation**: what to return when the simulation time is *outside* the
 * time interval covered by the data. Built-in implementations in [TemporalExtrapolation].
 */
fun interface TemporalExtrapolationStrategy {

    /**
     * @param currentTime current simulation time (as a `Double`), outside the range of [sliceTimes].
     * @param sliceTimes times of the available slices, ascending. For custom strategies that
     * need the actual instants.
     * @param sampleSlice given a slice index, returns that slice's value **already spatially sampled**
     * at the requested position.
     * @return the extrapolated value.
     */
    fun valueAt(currentTime: Double, sliceTimes: List<Double>, sampleSlice: (sliceIndex: Int) -> Double): Double
}

/**
 * Built-in temporal extrapolation strategies.
 */
enum class TemporalExtrapolation : TemporalExtrapolationStrategy {

    /**
     * Holds the value of the **last** available slice.
     */
    LAST {
        override fun valueAt(
            currentTime: Double,
            sliceTimes: List<Double>,
            sampleSlice: (sliceIndex: Int) -> Double,
        ): Double = sampleSlice(sliceTimes.lastIndex)
    },

    /**
     * Holds the value of the **first** available slice.
     */
    FIRST {
        override fun valueAt(
            currentTime: Double,
            sliceTimes: List<Double>,
            sampleSlice: (sliceIndex: Int) -> Double,
        ): Double = sampleSlice(0)
    },
}
