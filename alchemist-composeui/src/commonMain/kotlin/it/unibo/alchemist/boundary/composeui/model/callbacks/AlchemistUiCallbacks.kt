/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model.callbacks

import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode

/**
 * What the UI asks of the simulation, by area.
 * Changes that only touch the UI state, such as typing in a box or selecting nodes,
 * are not here: see [it.unibo.alchemist.boundary.composeui.view.state.UiEdits].
 * Failures are reported by setting [it.unibo.alchemist.boundary.composeui.model.SimulationControlsState.dialog],
 * unless a function returns them.
 */
internal interface AlchemistUiCallbacks {
    /** Running, pausing and jumping. */
    val transport: Transport

    /** The simulation speed limit. */
    val pace: Pace

    /** Changes to the nodes. */
    val nodes: Nodes

    /** What the simulation sends to draw. */
    val display: Display

    /** Running, pausing and jumping. Every jump ends paused. */
    interface Transport {
        /** Plays. */
        suspend fun play()

        /** Pauses. */
        suspend fun pause()

        /** Runs one step. */
        suspend fun step()

        /** Runs to time [target]. */
        suspend fun runToTime(target: Double)

        /** Runs to step [target]. */
        suspend fun runToStep(target: Long)

        /** Runs [delta] time units past the current time. */
        suspend fun jumpTimeBy(delta: Double)

        /** Runs [delta] steps past the current step. */
        suspend fun jumpStepBy(delta: Long)
    }

    /** The simulation speed limit. */
    interface Pace {
        /** Paces the simulation by [mode], at its default rate. */
        suspend fun setMode(mode: PacingMode)

        /** The rate box changed to [value], applied at once if it is a positive number. */
        suspend fun setRateInput(value: String)
    }

    /** Changes to the nodes, run between two simulation steps. */
    interface Nodes {
        /** Moves nodes to [nodePositions], when a drag ends. */
        suspend fun move(nodePositions: List<NodePositionUpdate>)

        /** Adds a copy of each node in [copies], with its contents, at the position given for it. */
        suspend fun clone(copies: List<NodePositionUpdate>)

        /** Removes the nodes [nodeIds] from the simulation. */
        suspend fun delete(nodeIds: List<Int>)

        /**
         * Sets [molecule] to [value] in the nodes [nodeIds], adding it where missing.
         * The simulation parses both, as its incarnation does for simulation files.
         * A failure is returned, not shown: the caller decides how to tell the user.
         */
        suspend fun setConcentration(nodeIds: List<Int>, molecule: String, value: String): Result<Unit>

        /** Removes [molecule] from the nodes [nodeIds] that contain it. */
        suspend fun removeMolecule(nodeIds: List<Int>, molecule: String)
    }

    /** What the simulation sends to draw. */
    interface Display {
        /** Sends the reactions of [nodeIds]: frames carry them only for the nodes selected when taken. */
        suspend fun inspect(nodeIds: List<Int>)

        /** Shows or hides links. */
        suspend fun toggleLinks()
    }
}
