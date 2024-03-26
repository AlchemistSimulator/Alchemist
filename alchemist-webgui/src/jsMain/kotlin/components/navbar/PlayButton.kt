/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.navbar

import graphql.api.SimulationControlApi
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.ButtonType
import io.kvision.state.bind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import stores.EnvironmentStore
import stores.SimulationStatus
import utils.SimState

/**
 * Class representing a custom play button for controlling simulation state.
 * This class extends the Button class and provides functionality to play, pause, or terminate a simulation.
 *
 * @param text the text to be displayed on the button
 */
class PlayButton(text: String) : Button(text) {

    private var simulationStatus = SimState.TERMINATED
    private var simulationRunning = false

    init {
        SimulationStatus.callGetStatus()

        type = ButtonType.BUTTON
        labelFirst = false

        this.bind(SimulationStatus.simulationStore) { sim ->
            simulationStatus = SimState.toSimStatus(sim.status?.simulationStatus)
            simulationRunning = simulationStatus == SimState.RUNNING

            this.text = when (simulationStatus) {
                SimState.TERMINATED -> "Terminated"
                SimState.RUNNING -> "Pause"
                else -> "Play"
            }

            icon = when (simulationStatus) {
                SimState.TERMINATED -> "fa-solid fa-ban"
                SimState.RUNNING -> "fa-solid fa-pause"
                else -> "fa-solid fa-play"
            }

            style = if (simulationRunning) ButtonStyle.DANGER else ButtonStyle.SUCCESS
            disabled = when (simulationStatus) {
                SimState.TERMINATED -> true
                else -> false
            }
        }

        onClick {
            CoroutineScope(Dispatchers.Default).launch {
                if (simulationStatus == SimState.READY || simulationStatus == SimState.PAUSED) {
                    SimulationControlApi.playSimulation()
                    EnvironmentStore.callEnvironmentSubscription()
                } else if (simulationStatus == SimState.RUNNING) {
                    SimulationControlApi.pauseSimulation()
                    // EnvironmentStore.cancelSubscription()
                }
                SimulationStatus.callGetStatus()
            }
        }
    }
}
