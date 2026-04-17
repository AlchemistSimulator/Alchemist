/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext

class DesktopAlchemistUiCallback<T, P : Position<P>>(
    private val simulation: Simulation<T, P>,
    private val store: ComposeUiStateStore
) : AlchemistUiCallbacks {
    override suspend fun onPlay() = coroutineScope {
        simulation.play().await()
        withContext(Dispatchers.Main) {
            store.update { it.copy(controls = it.controls.copy(status = SimulationStatus.RUNNING)) }
        }
    }

    override suspend fun onPause() = coroutineScope {
        simulation.pause().await()
        withContext(Dispatchers.Main) {
            store.update { it.copy(controls = it.controls.copy(status = SimulationStatus.PAUSED)) }
        }
    }

    override suspend fun onStep() {
        TODO("Not yet implemented")
    }

    override suspend fun onNodeSelected(nodeId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun onInspectorDismiss() {
        TODO("Not yet implemented")
    }
}
