/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max

/**
 * Slows the simulation thread down to a given event rate.
 * The wait between events can be cut short with [wakeUp], so that commands such as pause or terminate,
 * which the engine runs between steps, never wait for it.
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
            val intervalNs = (TimeUnit.SECONDS.toNanos(1) / eventsPerSecond).coerceAtLeast(1L)
            nextReleaseNs = max(System.nanoTime(), nextReleaseNs) + intervalNs
            var remainingNs = nextReleaseNs - System.nanoTime()
            while (remainingNs > 0L && !wokenUp) {
                remainingNs = wakeUpSignal.awaitNanos(remainingNs)
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
}
