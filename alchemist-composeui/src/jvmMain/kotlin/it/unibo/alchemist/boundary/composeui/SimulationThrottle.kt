/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.pacing.Pacing
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Slows the simulation thread down to a [Pacing]: [dueAt] tells when an event is due, and [sleepUntil] waits for it.
 * Each event has a due date, counted from the first event at the current pace:
 * the thread waits only once it is at least [MIN_WAIT_NS] ahead of it,
 * since shorter waits are not precise enough, and would cap fast rates far below the requested one.
 * A simulation more than [MAX_CATCH_UP_NS] behind starts counting again, rather than rushing to catch up.
 * The wait can be cut short with [wakeUp], so that commands such as pause or terminate,
 * which the engine runs between steps, never wait for it.
 * It blocks rather than suspends: the engine runs on its own thread and calls monitors synchronously.
 */
internal class SimulationThrottle {
    private val lock = ReentrantLock()
    private val wakeUpSignal = lock.newCondition()
    private var pacing: Pacing? = null
    private var startNs = 0L
    private var startTime = 0.0
    private var events = 0L
    private var wokenUp = false

    /**
     * Returns when the event that reached simulated [time] is due at [pace], as a [System.nanoTime] value.
     */
    fun dueAt(pace: Pacing, time: Double): Long = lock.withLock {
        val now = System.nanoTime()
        if (pace != pacing) {
            restart(pace, now, time)
        }
        events++
        val dueNs = startNs + (pace.secondsFor(events, time - startTime) * 1.seconds.inWholeNanoseconds).roundToLong()
        if (dueNs < now - MAX_CATCH_UP_NS) {
            restart(pace, now, time)
            now
        } else {
            dueNs
        }
    }

    /** Waits until [dueNs], or until [wakeUp] is called. */
    fun sleepUntil(dueNs: Long) = lock.withLock {
        var remainingNs = dueNs - System.nanoTime()
        if (remainingNs >= MIN_WAIT_NS) {
            while (remainingNs > 0L && !wokenUp) {
                remainingNs = wakeUpSignal.awaitNanos(remainingNs)
            }
        }
        wokenUp = false
    }

    /** Forgets the pace so far: the next event runs without waiting. */
    fun reset() = lock.withLock {
        pacing = null
    }

    /** Ends the current wait, if any, and forgets the pace so far. */
    fun wakeUp() = lock.withLock {
        wokenUp = true
        pacing = null
        wakeUpSignal.signalAll()
    }

    private fun restart(pace: Pacing, now: Long, time: Double) {
        pacing = pace
        startNs = now
        startTime = time
        events = 0L
    }

    private companion object {
        /** Shorter waits are skipped, and made up for by a longer one later. */
        val MIN_WAIT_NS: Long = 1.milliseconds.inWholeNanoseconds

        /** How far behind its pace a slow simulation may fall and still catch up. */
        val MAX_CATCH_UP_NS: Long = 100.milliseconds.inWholeNanoseconds
    }
}
