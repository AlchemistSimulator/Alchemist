/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.snapshots

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState

/**
 * One snapshot of a scenario, and what to do in the UI before taking it.
 *
 * @property name the end of the file name: `<scenario>-<name>.png`.
 * @property width the window width, in pixels.
 * @property height the window height, in pixels.
 * @property select the ids of the nodes to select, which opens the inspector.
 * @property editor the molecule to open in the large value editor, for the selected nodes.
 * @property zoom wheel notches at the centre of the window: positive zooms in, negative out.
 * @property pan how far to drag the view with the right button, in pixels.
 * @property keys the keys to press, in order, with the viewport focused.
 */
internal data class SnapshotSpec(
    val name: String,
    val width: Int = WIDE_WIDTH,
    val height: Int = WIDE_HEIGHT,
    val select: List<Int> = emptyList(),
    val editor: String? = null,
    val zoom: Int = 0,
    val pan: Offset = Offset.Zero,
    val keys: List<Key> = emptyList(),
) {
    /** [state] with the selection and the value editor of this snapshot. */
    fun applyTo(state: AlchemistUiState): AlchemistUiState = state
        .withSelection(select)
        .let { selected -> editor?.let { selected.withValueEditorToggled(select, it) } ?: selected }

    internal companion object {
        private const val WIDE_WIDTH = 1920
        private const val WIDE_HEIGHT = 1080
        private const val COMPACT_WIDTH = 1024
        private const val COMPACT_HEIGHT = 600

        /** The snapshots of a scenario that lists none: wide, compact, and wide with four nodes selected. */
        val DEFAULTS = listOf(
            SnapshotSpec(name = "wide"),
            SnapshotSpec(name = "compact", width = COMPACT_WIDTH, height = COMPACT_HEIGHT),
            SnapshotSpec(name = "inspector", select = listOf(0, 1, 2, 3)),
        )

        /** The snapshots of each scenario, by file name without `.alchemist.kts`, when not the [DEFAULTS]. */
        val BY_SCENARIO = mapOf(
            "dense" to listOf(
                SnapshotSpec(name = "wide"),
                SnapshotSpec(name = "inspector", select = (0..7).toList()),
            ),
            "grid" to DEFAULTS + SnapshotSpec(
                name = "zoomed-editor-help",
                select = listOf(0),
                editor = "distance",
                zoom = 4,
                pan = Offset(300f, -200f),
                keys = listOf(Key.H),
            ),
        )
    }
}
