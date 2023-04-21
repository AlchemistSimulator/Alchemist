/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.test.GlobalTestReaction
import it.unibo.alchemist.testsupport.createEmptyEnvironment
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TestSimulationControl : FreeSpec(
    {
        "The progress of the simulation must be controllable" - {
            "goToTime should move the simulation to the specified time" {
                val environment = createEmptyEnvironment<Nothing>()
                environment.tickRate(1.0)
                val jump = DoubleTime(10.0)
                with(environment.simulation) {
                    goToTime(jump)
                    play()
                    workerPool.submit { run() }
                    waitFor(Status.PAUSED, 1, TimeUnit.SECONDS)
                    terminate()
                    time shouldBe jump
                }
            }
            "goToTime should work twice" {
                val environment = createEmptyEnvironment<Nothing>()
                val awaitForNextJump = 100L
                environment.tickRate(1.0)
                val firstJump = DoubleTime(10.0)
                val nextJump = DoubleTime(20.0)
                with(environment.simulation) {
                    goToTime(firstJump)
                    play()
                    workerPool.submit { run() }
                    waitFor(Status.PAUSED, 1, TimeUnit.SECONDS)
                    time shouldBe firstJump
                    goToTime(nextJump)
                    play()
                    // best solution so far for now, you cannot be sure that the simulation is played again
                    delay(awaitForNextJump)
                    waitFor(Status.PAUSED, 1, TimeUnit.SECONDS)
                    terminate()
                    time shouldBe nextJump
                }
            }
        }
    },
) {
    companion object {
        val workerPool: ExecutorService = Executors.newCachedThreadPool()
        fun Environment<Nothing, *>.tickRate(delta: Double) {
            this.simulation.schedule {
                this.addGlobalReaction(GlobalTestReaction(DiracComb(Time.ZERO, delta), this))
            }
        }
    }
}
