/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.unibo.alchemist.boundary.composeui.viewmodels.SimulationStatusViewModel

/**
 * Application entry point, this will be rendered the same in all the platforms.
 */
@Composable
fun app() {
    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            simulationStatus()
        }
    }
}

@Composable
fun simulationStatus(viewModel: SimulationStatusViewModel = viewModel { SimulationStatusViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Text("Simulation status: ${uiState.status}")
    Text("Simulation time: ${uiState.time}")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { viewModel.pause() }) {
            Text("Pause")
        }
        Button(onClick = { viewModel.play() }) {
            Text("Play")
        }
    }
}
