/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.state.edits

import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore

/** The "to time" and "to step" boxes in [store]. */
internal class JumpEdits(private val store: ComposeUiStateStore) {
    /** The "to step" box changed to [value]. */
    fun setStepInput(value: String) = store.update { it.copy(controls = it.controls.copy(toStepInput = value)) }

    /** The "to time" box changed to [value]. */
    fun setTimeInput(value: String) = store.update { it.copy(controls = it.controls.copy(toTimeInput = value)) }

    /** The step in the "to step" box, or null after explaining why there is none. */
    fun stepTarget(): Long? = store.state.controls.targetStep.also { target ->
        if (target == null) {
            store.update { it.withDialog("Invalid step", "Insert a valid integer step.") }
        }
    }

    /** The time in the "to time" box, or null after explaining why there is none. */
    fun timeTarget(): Double? = store.state.controls.targetTime.also { target ->
        if (target == null) {
            store.update { it.withDialog("Invalid time", "Insert a valid numeric time.") }
        }
    }
}
