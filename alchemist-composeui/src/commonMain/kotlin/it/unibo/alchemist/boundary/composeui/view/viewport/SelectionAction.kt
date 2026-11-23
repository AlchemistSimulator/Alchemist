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
 * A button shown over the selected nodes.
 *
 * @property label the text on the button.
 * @property description what the button does, for accessibility.
 */
internal enum class SelectionAction(val label: String, val description: String) {
    /** Drags the selection to a new place. */
    MOVE("Move", "Move the selection"),

    /** Drags copies of the selection to a new place, leaving the originals. */
    CLONE("Clone", "Clone the selection"),

    /** Removes the selection from the simulation. */
    DELETE("Delete", "Delete the selection"),
}
