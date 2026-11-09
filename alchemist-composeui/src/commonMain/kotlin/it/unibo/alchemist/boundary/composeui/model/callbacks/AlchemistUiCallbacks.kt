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

/**
 * User actions, sent by the UI to the simulation.
 * The UI talks to the simulation only through these.
 * Invalid inputs are reported by setting [it.unibo.alchemist.boundary.composeui.model.SimulationControlsState.dialog].
 */
interface AlchemistUiCallbacks {
    /** Plays. */
    suspend fun onPlay()

    /** Pauses. */
    suspend fun onPause()

    /** Runs one step, then pauses. */
    suspend fun onStep()

    /** The "to time" box changed to [value]. */
    suspend fun onToTimeInputChanged(value: String)

    /** Runs to the time in the "to time" box. */
    suspend fun onToTimeSubmit()

    /** The "to step" box changed to [value]. */
    suspend fun onToStepInputChanged(value: String)

    /** Runs to the step in the "to step" box. */
    suspend fun onToStepSubmit()

    /** The FPS box changed to [value]. */
    suspend fun onFpsInputChanged(value: String)

    /** Applies the frame rate in the FPS box. */
    suspend fun onFpsSubmit()

    /** The speed slider moved to [value]. */
    suspend fun onEventRateChanged(value: Float)

    /** Selects only node [nodeId]. */
    suspend fun onNodeSelected(nodeId: Int)

    /** Selects exactly [nodeIds]. */
    suspend fun onNodesSelected(nodeIds: List<Int>)

    /**
     * Moves nodes to [nodePositions] when a drag ends.
     * During the drag, the UI only previews the move.
     */
    suspend fun onNodesMoved(nodePositions: List<NodePositionUpdate>)

    /** Closes the inspector and clears the selection. */
    suspend fun onInspectorDismiss()

    /** Shows or hides links. */
    suspend fun onToggleLinks()

    /** Closes the error dialog. */
    suspend fun onDialogDismiss()
}
