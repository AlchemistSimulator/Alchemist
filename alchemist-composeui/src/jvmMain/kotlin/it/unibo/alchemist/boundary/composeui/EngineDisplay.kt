/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.reactionsOf
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.toViewport
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.model.Position

/**
 * What the simulation of [bridge] sends to draw.
 */
internal class EngineDisplay<T, P : Position<P>>(private val bridge: EngineBridge<T, P>) :
    AlchemistUiCallbacks.Display {
    override suspend fun inspect(nodeIds: List<Int>) {
        val reactions = bridge.read { reactionsOf(nodeIds) }
        bridge.updateState {
            it.copy(scene = it.scene.withMissingReactions(reactions)).withSelection(it.selectedNodeIds)
        }
    }

    override suspend fun toggleLinks() = bridge.updateState {
        val showLinks = !it.scene.showLinks
        val scene = bridge.simulation.environment.toViewport(renderLinks = showLinks, inspected = it.selectedNodeIds)
        it.copy(scene = scene.copy(showLinks = showLinks)).withSelection(it.selectedNodeIds)
    }
}
