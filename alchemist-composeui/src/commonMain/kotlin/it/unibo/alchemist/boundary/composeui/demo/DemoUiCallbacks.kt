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
 * Fake simulation controls for the demo scene, used when no simulation is attached.
 */
internal class DemoUiCallbacks(store: ComposeUiStateStore) : AlchemistUiCallbacks {
    override val transport: AlchemistUiCallbacks.Transport = DemoTransport(store)
    override val pace: AlchemistUiCallbacks.Pace = DemoPace(store)
    override val nodes: AlchemistUiCallbacks.Nodes = DemoNodes(store)
    override val display: AlchemistUiCallbacks.Display = DemoDisplay(store)
}
