/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.logic

import it.unibo.alchemist.boundary.webui.client.api.EnvironmentApi
import it.unibo.alchemist.boundary.webui.client.api.SimulationApi.getSimulationStatus
import it.unibo.alchemist.boundary.webui.client.state.ClientStore.store
import it.unibo.alchemist.boundary.webui.client.state.actions.SetBitmap
import it.unibo.alchemist.boundary.webui.client.state.actions.SetStatusSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate
import korlibs.image.bitmap.Bitmap
import korlibs.io.async.launch
import kotlinx.coroutines.Dispatchers

/**
 * Update the application state using HTTP calls to the REST server.
 */
class RESTUpdateStateStrategy : UpdateStateStrategy {

    /**
     * Retrieve the Environment in serialized form using the [EnvironmentApi].
     * The Environment will be rendered and saved in the
     * [it.unibo.alchemist.boundary.webui.client.state.ClientStore].
     */
    override suspend fun clientComputation() {
        val environment: EnvironmentSurrogate<Any, PositionSurrogate> = EnvironmentApi.getEnvironmentClient()
        launch(Dispatchers.Default) {
            storeBitmap(store.state.renderer.render(environment))
        }
    }

    /**
     * Retrieve the Environment in already rendered form using the [EnvironmentApi].
     * Decode it and save it in the
     * [it.unibo.alchemist.boundary.webui.client.state.ClientStore].
     */
    override suspend fun serverComputation() {
        storeBitmap(EnvironmentApi.getEnvironmentServer())
    }

    /**
     * Retrieve the Simulation Status as a
     * [it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate],
     * then save it in
     * the [it.unibo.alchemist.boundary.webui.client.state.ClientStore].
     */
    override suspend fun retrieveSimulationStatus() {
        store.dispatch(SetStatusSurrogate(getSimulationStatus()))
    }

    private fun storeBitmap(bitmap: Bitmap) {
        store.dispatch(SetBitmap(bitmap))
    }
}
