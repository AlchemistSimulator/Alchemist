/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.test.GlobalTestReaction
import it.unibo.alchemist.test.createEmptyEnvironment
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
                    val jumpFuture = goToTime(jump)
                    play()
                    workerPool.submit { run() }
                    jumpFuture.get(1, TimeUnit.SECONDS)
                    terminate()
                    time shouldBe jump
                }
            }
            "goToTime should work twice" {
                val environment = createEmptyEnvironment<Nothing>()
                environment.tickRate(1.0)
                val firstJump = DoubleTime(10.0)
                val nextJump = DoubleTime(20.0)
                with(environment.simulation) {
                    val firstJumpFuture = goToTime(firstJump)
                    play()
                    val secondJump = firstJumpFuture
                        .thenCompose { goToTime(nextJump) }
                    // concurrent w.r.t. second jump
                    firstJumpFuture.thenRun {
                        play()
                    }
                    firstJumpFuture.thenRun { time shouldBe firstJump }
                    workerPool.submit { run() }
                    secondJump.get(1, TimeUnit.SECONDS)
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
