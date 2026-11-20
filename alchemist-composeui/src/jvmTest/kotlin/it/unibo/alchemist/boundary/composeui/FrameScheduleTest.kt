/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FrameScheduleTest {
    @Test
    fun `the first step is always shown`() {
        assertTrue(FrameSchedule().isDue(nowNs = 0, idleNs = 0, fps = FPS))
    }

    @Test
    fun `snapshots are at most one per frame`() {
        val frames = FrameSchedule().apply { taken(startNs = 0, endNs = 1, fps = FPS) }
        assertFalse(frames.isDue(nowNs = FRAME_NS, idleNs = 0, fps = FPS))
        assertTrue(frames.isDue(nowNs = FRAME_NS + 1, idleNs = 0, fps = FPS))
    }

    @Test
    fun `slow snapshots leave the simulation as much time as they take`() {
        val frames = FrameSchedule().apply { taken(startNs = 0, endNs = 3 * FRAME_NS, fps = FPS) }
        assertFalse(frames.isDue(nowNs = 5 * FRAME_NS, idleNs = 0, fps = FPS))
        assertTrue(frames.isDue(nowNs = 6 * FRAME_NS, idleNs = 0, fps = FPS))
    }

    @Test
    fun `a step followed by a long wait is shown at once`() {
        val frames = FrameSchedule().apply { taken(startNs = 0, endNs = 1, fps = FPS) }
        assertTrue(frames.isDue(nowNs = 2, idleNs = FRAME_NS, fps = FPS))
    }

    private companion object {
        const val FPS = 10
        const val FRAME_NS = 100_000_000L
    }
}
