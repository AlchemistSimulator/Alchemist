/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestTemporalExtrapolation : StringSpec({

    /*
     * Simple recording callback: it remembers the index it was asked for, and returns a value
     * derived from that index so a single assertion proves both which index was sampled and
     * that the strategy returns the callback's result.
     */
    class IndexRecorder {
        var sampledIndex: Int? = null
            private set

        val sampleSlice: (Int) -> Double = { index ->
            sampledIndex = index
            index * 10.0
        }
    }

    val sliceTimes = listOf(100.0, 200.0, 300.0, 400.0)

    // out of range probe times: one before the first slice, one after the last.
    val timeBeforeRange = 50.0
    val timeAfterRange = 500.0

    "LAST samples the last slice index regardless of the current time" {
        for (currentTime in listOf(timeBeforeRange, timeAfterRange)) {
            val recorder = IndexRecorder()
            withClue("at time $currentTime") {
                TemporalExtrapolation.LAST.valueAt(
                    currentTime,
                    sliceTimes,
                    recorder.sampleSlice,
                ) shouldBe sliceTimes.lastIndex * 10
                recorder.sampledIndex shouldBe sliceTimes.lastIndex
            }
        }
    }

    "FIRST samples the first slice index regardless of the current time" {
        for (currentTime in listOf(timeBeforeRange, timeAfterRange)) {
            val recorder = IndexRecorder()
            withClue("at time $currentTime") {
                TemporalExtrapolation.FIRST.valueAt(
                    currentTime,
                    sliceTimes,
                    recorder.sampleSlice,
                ) shouldBe 0.0
                recorder.sampledIndex shouldBe 0
            }
        }
    }

    "LAST and FIRST both sample index 0 when there is a single slice" {
        val singleTime = listOf(100.0)
        val lastRecorder = IndexRecorder()
        val firstRecorder = IndexRecorder()

        TemporalExtrapolation.LAST.valueAt(
            timeAfterRange,
            singleTime,
            lastRecorder.sampleSlice,
        )
        TemporalExtrapolation.FIRST.valueAt(
            timeBeforeRange,
            singleTime,
            firstRecorder.sampleSlice,
        )

        lastRecorder.sampledIndex shouldBe 0
        firstRecorder.sampledIndex shouldBe 0
    }
})
