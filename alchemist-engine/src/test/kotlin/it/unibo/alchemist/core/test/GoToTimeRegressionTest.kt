/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.test

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.test.AlchemistTesting.createEmptyEnvironment
import it.unibo.alchemist.test.GlobalTestReaction
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

/**
 * Regression test for the goToTime issue #2017.
 * Ensures that OutputMonitors are properly removed after condition satisfaction,
 * preventing old monitors from triggering on subsequent goToTime calls.
 */
object GoToTimeRegressionTest {

    private val workerPool: ExecutorService = Executors.newCachedThreadPool()

    private fun Environment<Nothing, *>.tickRate(delta: Double) {
        this.simulation.schedule {
            this.addGlobalReaction(GlobalTestReaction(this, DiracComb(Time.ZERO, delta)))
        }
    }

    @Test
    fun `monitors should not interfere with subsequent goToTime calls`() {
        val environment = createEmptyEnvironment<Nothing>()
        environment.tickRate(1.0)
        with(environment.simulation) {
            // First goToTime to 5.0
            val firstTarget = DoubleTime(5.0)
            val firstJump = goToTime(firstTarget)
            play()
            workerPool.submit { run() }
            firstJump.get(3, TimeUnit.SECONDS)
            val timeAfterFirst = time
            assertTrue(timeAfterFirst.toDouble() >= firstTarget.toDouble())
            // Second goToTime to 15.0 - this should work without old monitor interference
            val secondTarget = DoubleTime(15.0)
            val secondJump = goToTime(secondTarget)
            play()
            secondJump.get(3, TimeUnit.SECONDS)
            val timeAfterSecond = time
            assertTrue(timeAfterSecond.toDouble() >= secondTarget.toDouble())
            // Third goToTime to 25.0 - testing multiple sequential calls
            val thirdTarget = DoubleTime(25.0)
            val thirdJump = goToTime(thirdTarget)
            play()
            thirdJump.get(3, TimeUnit.SECONDS)
            val timeAfterThird = time
            assertTrue(timeAfterThird.toDouble() >= thirdTarget.toDouble())
            terminate()
            // Verify final state
            assertEquals(thirdTarget, time)
        }
    }

    @Test
    fun `monitor count should remain stable across multiple goToTime calls`() {
        val environment = createEmptyEnvironment<Nothing>()
        environment.tickRate(1.0)
        with(environment.simulation) {
            val initialMonitorCount = outputMonitors.size
            // First jump
            val firstJump = goToTime(DoubleTime(5.0))
            play()
            workerPool.submit { run() }
            firstJump.get(3, TimeUnit.SECONDS)
            val afterFirstCount = outputMonitors.size
            // Second jump
            val secondJump = goToTime(DoubleTime(10.0))
            play()
            secondJump.get(3, TimeUnit.SECONDS)
            val afterSecondCount = outputMonitors.size
            terminate()
            // Monitor count should return to initial state
            // (may have +/- 1 due to internal simulation monitors, but should be stable)
            assertEquals(initialMonitorCount, afterFirstCount)
            assertEquals(initialMonitorCount, afterSecondCount)
        }
    }
}
