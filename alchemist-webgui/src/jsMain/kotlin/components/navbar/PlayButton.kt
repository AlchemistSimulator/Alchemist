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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import stores.SimulationStatus
import utils.SimState

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
            MainScope().launch {
                println("Button clicked coroutine")

                when (simulationStatus) {
                    SimState.READY -> {
                        SimulationControlApi.playSimulation()
                    }
                    SimState.PAUSED -> {

                        SimulationControlApi.playSimulation()
                    }
                    SimState.RUNNING -> {
                        SimulationControlApi.pauseSimulation()
                    }
                    else -> {
                        SimulationControlApi.terminateSimulation()
                    }
                }
                SimulationStatus.callGetStatus()
            }
        }
    }
}
