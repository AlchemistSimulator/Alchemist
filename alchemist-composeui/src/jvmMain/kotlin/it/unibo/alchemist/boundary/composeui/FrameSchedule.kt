/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

/**
 * Decides when the simulation thread takes a snapshot for the UI.
 * Snapshots are at most one per frame, and leave the simulation at least as much time as they take.
 * A step followed by a wait of a frame or more is always shown, since the snapshot then costs nothing.
 * Times are in nanoseconds, as given by [System.nanoTime].
 */
internal class FrameSchedule {
    private var nextNs: Long? = null

    /** Whether to take a snapshot at [nowNs], before the simulation waits [idleNs], with a cap of [fps]. */
    fun isDue(nowNs: Long, idleNs: Long, fps: Int): Boolean =
        nextNs.let { next -> next == null || nowNs >= next || idleNs >= frameNs(fps) }

    /** Records a snapshot taken from [startNs] to [endNs], with a cap of [fps]. */
    fun taken(startNs: Long, endNs: Long, fps: Int) {
        nextNs = endNs + max(frameNs(fps), endNs - startNs)
    }

    private fun frameNs(fps: Int): Long = 1.seconds.inWholeNanoseconds / fps
}
