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

        /** The keys a snapshot can press, by name. */
        private val KEYS = mapOf("C" to Key.C, "H" to Key.H, "Escape" to Key.Escape)

        /** The snapshots of a scenario that lists none: wide, compact, and wide with four nodes selected. */
        val DEFAULTS = listOf(
            SnapshotSpec(name = "wide"),
            SnapshotSpec(name = "compact", width = COMPACT_WIDTH, height = COMPACT_HEIGHT),
            SnapshotSpec(name = "inspector", select = listOf(0, 1, 2, 3)),
        )

        /**
         * The snapshot described by [entry], an item of the `_snapshots` list of a scenario:
         * `name` is required, everything else is optional, as in [SnapshotSpec].
         */
        fun of(entry: Map<*, *>): SnapshotSpec {
            val size = entry["size"]?.numbers()
            val pan = entry["pan"]?.numbers()
            return SnapshotSpec(
                name = requireNotNull(entry["name"]?.toString()) { "Every snapshot needs a name: $entry" },
                width = size?.get(0)?.toInt() ?: WIDE_WIDTH,
                height = size?.get(1)?.toInt() ?: WIDE_HEIGHT,
                select = entry["select"]?.numbers()?.map(Number::toInt).orEmpty(),
                editor = entry["editor"]?.toString(),
                zoom = (entry["zoom"] as? Number)?.toInt() ?: 0,
                pan = pan?.let { Offset(it[0].toFloat(), it[1].toFloat()) } ?: Offset.Zero,
                keys = (entry["keys"] as? List<*>).orEmpty().map { name ->
                    requireNotNull(KEYS[name.toString()]) { "Unknown key $name: use one of ${KEYS.keys}" }
                },
            )
        }

        private fun Any.numbers(): List<Number> =
            (this as? List<*>)?.map { it as? Number ?: error("Not a number: $it in $this") }
                ?: error("Not a list of numbers: $this")
    }
}
