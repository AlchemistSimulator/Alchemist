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
 * Shared no-op callback implementation.
 */
internal object NoOpUiCallbacks : AlchemistUiCallbacks {
    override suspend fun onPlay() = Unit

    override suspend fun onPause() = Unit

    override suspend fun onStep() = Unit

    override suspend fun onToTimeInputChanged(value: String) = Unit

    override suspend fun onToTimeSubmit() = Unit

    override suspend fun onToStepInputChanged(value: String) = Unit

    override suspend fun onToStepSubmit() = Unit

    override suspend fun onFpsInputChanged(value: String) = Unit

    override suspend fun onFpsSubmit() = Unit

    override suspend fun onEventRateChanged(value: Float) = Unit

    override suspend fun onNodeSelected(nodeId: Int) = Unit

    override suspend fun onNodesSelected(nodeIds: List<Int>) = Unit

    override suspend fun onNodesMoved(nodePositions: List<NodePositionUpdate>) = Unit

    override suspend fun onInspectorDismiss() = Unit

    override suspend fun onToggleLinks() = Unit

    override suspend fun onDialogDismiss() = Unit
}
