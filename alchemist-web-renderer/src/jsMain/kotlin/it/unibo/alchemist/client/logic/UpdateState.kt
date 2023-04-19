/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.logic

import it.unibo.alchemist.client.state.ClientStore.store
import it.unibo.alchemist.common.model.RenderMode
import it.unibo.alchemist.common.model.surrogate.StatusSurrogate

/**
 * Update the application state, including the Environment.
 * @param renderMode the render mode set by the user.
 * @param updateStateStrategy the [UpdateStateStrategy] to use.
 * @param autoStrategy the [AutoRenderModeStrategy] to use.
 */
suspend fun updateState(
    renderMode: RenderMode,
    updateStateStrategy: UpdateStateStrategy,
    autoStrategy: AutoRenderModeStrategy,
) {
    updateStateStrategy.retrieveSimulationStatus()
    if (store.state.statusSurrogate == StatusSurrogate.RUNNING || store.state.bitmap == null) {
        updateEnvironment(renderMode, updateStateStrategy, autoStrategy)
    }
}

/**
 * Update the state of the Environment.
 * @param renderMode the render mode set by the user.
 * @param updateStateStrategy the [UpdateStateStrategy] to use.
 * @param autoStrategy the [AutoRenderModeStrategy] to use.
 */
private suspend fun updateEnvironment(
    renderMode: RenderMode,
    updateStateStrategy: UpdateStateStrategy,
    autoStrategy: AutoRenderModeStrategy,
): Unit = when (renderMode) {
    RenderMode.CLIENT -> updateStateStrategy.clientComputation()
    RenderMode.SERVER -> updateStateStrategy.serverComputation()
    RenderMode.AUTO -> updateEnvironment(
        autoStrategy(),
        updateStateStrategy,
        object : AutoRenderModeStrategy {
            override fun invoke(): RenderMode = error("Auto mode cannot be returned here.")
        },
    )
}
