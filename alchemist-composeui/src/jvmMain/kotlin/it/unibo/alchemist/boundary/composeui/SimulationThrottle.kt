/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Slows the simulation thread down to a given event rate.
 * Events follow a schedule, and the thread waits only once it is at least [MIN_WAIT_NS] ahead of it:
 * shorter waits are not precise enough, and would cap fast rates far below the requested one.
 * A simulation that falls behind may catch up for at most [MAX_CATCH_UP_NS], without waiting.
 * The wait can be cut short with [wakeUp], so that commands such as pause or terminate,
 * which the engine runs between steps, never wait for it.
 * It blocks rather than suspends: the engine runs on its own thread and calls monitors synchronously.
 */
internal class SimulationThrottle {
    private val lock = ReentrantLock()
    private val wakeUpSignal = lock.newCondition()
    private var nextReleaseNs = 0L
    private var wokenUp = false

    /**
     * Waits until the next event may run at [eventsPerSecond], or until [wakeUp] is called.
     * A null rate means no limit.
     */
    fun await(eventsPerSecond: Int?) = lock.withLock {
        if (eventsPerSecond == null) {
            nextReleaseNs = 0L
        } else {
            val intervalNs = (1.seconds.inWholeNanoseconds / eventsPerSecond).coerceAtLeast(1L)
            val now = System.nanoTime()
            val scheduled = if (nextReleaseNs == 0L) now else max(nextReleaseNs, now - MAX_CATCH_UP_NS)
            nextReleaseNs = scheduled + intervalNs
            var remainingNs = nextReleaseNs - now
            if (remainingNs >= MIN_WAIT_NS) {
                while (remainingNs > 0L && !wokenUp) {
                    remainingNs = wakeUpSignal.awaitNanos(remainingNs)
                }
            }
        }
        wokenUp = false
    }

    /** Forgets the pace so far: the next event runs without waiting. */
    fun reset() = lock.withLock {
        nextReleaseNs = 0L
    }

    /** Ends the current wait, if any. */
    fun wakeUp() = lock.withLock {
        wokenUp = true
        nextReleaseNs = 0L
        wakeUpSignal.signalAll()
    }

    private companion object {
        /** Shorter waits are skipped, and made up for by a longer one later. */
        val MIN_WAIT_NS: Long = 1.milliseconds.inWholeNanoseconds

        /** How far behind the schedule a slow simulation may fall and still catch up. */
        val MAX_CATCH_UP_NS: Long = 100.milliseconds.inWholeNanoseconds
    }
}
