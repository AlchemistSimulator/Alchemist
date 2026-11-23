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

/**
 * The selected nodes in [store], and the panels showing them: the inspector and the value editor.
 */
internal class SelectionEdits(private val store: ComposeUiStateStore) {
    /** Clears the selection, closing the inspector and the value editor. */
    fun clear() = select(emptyList())

    /** Closes the value editor. */
    fun closeValueEditor() = store.update { it.copy(valueEditor = null) }

    /** Opens [molecule] of the nodes [nodeIds] in the value editor, or closes it if it already shows them. */
    fun toggleValueEditor(nodeIds: List<Int>, molecule: String) =
        store.update { it.withValueEditorToggled(nodeIds, molecule) }

    /** Selects exactly [nodeIds]. */
    fun select(nodeIds: List<Int>) = store.update { it.withSelection(nodeIds) }
}
