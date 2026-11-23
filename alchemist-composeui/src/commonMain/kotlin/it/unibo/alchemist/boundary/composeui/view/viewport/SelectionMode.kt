/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.viewport

/**
 * How a click or a selection box changes the selected nodes.
 */
internal enum class SelectionMode {
    /** The nodes become the selection. */
    REPLACE,

    /** The nodes join the selection. */
    ADD,

    /** Each node joins the selection, or leaves it if it was there. */
    TOGGLE,

    ;

    /** The selection that applying [nodeIds] to [selected] gives. */
    fun apply(selected: Set<Int>, nodeIds: Collection<Int>): List<Int> = when (this) {
        REPLACE -> nodeIds.toList()
        ADD -> (selected + nodeIds).toList()
        TOGGLE -> {
            val toggled = nodeIds.toSet()
            selected.filterNot(toggled::contains) + toggled.filterNot(selected::contains)
        }
    }

    internal companion object {
        /** Shift adds to the selection, Ctrl toggles, no key replaces it. */
        fun of(ctrlPressed: Boolean, shiftPressed: Boolean): SelectionMode = when {
            shiftPressed -> ADD
            ctrlPressed -> TOGGLE
            else -> REPLACE
        }
    }
}
