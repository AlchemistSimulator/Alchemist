/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Top-level state consumed by the Compose UI shell.
 *
 * @property scene the viewport.
 * @property controls the control bar.
 * @property selectedNodeIds the selected node ids.
 * @property inspector the inspector; `null` if nothing is selected.
 */
@Immutable
internal data class AlchemistUiState(
    val scene: ViewportScene = ViewportScene(),
    val controls: SimulationControlsState = SimulationControlsState(),
    val selectedNodeIds: ImmutableList<Int> = persistentListOf(),
    val inspector: InspectorState? = null,
) {
    /** This state, showing a dialog. */
    internal fun withDialog(title: String, message: String): AlchemistUiState = copy(
        controls = controls.copy(dialog = ControlDialogState(title, message)),
    )

    /** This state, with [nodeIds] selected and the inspector updated. */
    internal fun withSelection(nodeIds: List<Int>): AlchemistUiState {
        val selectedIds = scene.sanitizeSelection(nodeIds)
        return copy(selectedNodeIds = selectedIds, inspector = scene.toInspectorState(selectedIds))
    }
}
