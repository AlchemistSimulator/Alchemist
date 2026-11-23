/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position

/**
 * UI callbacks that drive a [simulation] running in the same JVM and publish the resulting UI state to [store].
 * Every command wakes up the [throttle] first, so it never waits for the pause between events.
 */
internal class EngineUiCallbacks<T, P : Position<P>>(
    simulation: Simulation<T, P>,
    store: ComposeUiStateStore,
    throttle: SimulationThrottle = SimulationThrottle(),
) : AlchemistUiCallbacks {
    private val bridge = EngineBridge(simulation, store, throttle)
    override val transport: AlchemistUiCallbacks.Transport = EngineTransport(bridge)
    override val pace: AlchemistUiCallbacks.Pace = EnginePace(bridge, throttle)
    override val nodes: AlchemistUiCallbacks.Nodes = EngineNodes(bridge)
    override val display: AlchemistUiCallbacks.Display = EngineDisplay(bridge)
}
