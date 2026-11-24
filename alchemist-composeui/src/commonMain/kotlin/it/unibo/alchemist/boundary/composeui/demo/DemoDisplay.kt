/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.demo

import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore

/**
 * What the demo scene draws: its links are always available, so toggling them only flips the switch.
 */
internal class DemoDisplay(private val store: ComposeUiStateStore) : AlchemistUiCallbacks.Display {
    /** Demo nodes run no reactions. */
    override suspend fun inspect(nodeIds: List<Int>) = store.update {
        it.copy(scene = it.scene.withMissingReactions(nodeIds.associateWith { emptyList() }))
            .withSelection(it.selectedNodeIds)
    }

    override suspend fun toggleLinks() = store.update {
        it.copy(scene = it.scene.copy(showLinks = !it.scene.showLinks))
    }
}
