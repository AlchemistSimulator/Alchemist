/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content

import components.content.shared.CommonProperties
import org.w3c.dom.DOMRect
import org.w3c.dom.events.MouseEvent
import stores.EnvironmentStore
import stores.NodeStore
import utils.isMouseOverNodePosition

fun findNode(event: MouseEvent, boundingRect: DOMRect) {
    val state = CommonProperties.Observables.scaleTranslationStore.getState()

    val calcX = (event.clientX - boundingRect.left - state.translate.first) / state.scale
    val calcY = (event.clientY - boundingRect.top - state.translate.second) / state.scale

    val radius = CommonProperties.Observables.nodesRadius.value * 1 / state.scale

    val node = EnvironmentStore.store.getState().nodes.firstOrNull { entry ->
        isMouseOverNodePosition(calcX, calcY, entry.position.coordinates[0], entry.position.coordinates[1], radius)
    }
    if (node == null) return

    NodeStore.nodeById(node.id)
}
