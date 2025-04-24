/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.unibo.alchemist.boundary.composeui.viewmodels.SimulationStatus
import it.unibo.alchemist.boundary.composeui.viewmodels.SimulationStatusViewModel

/**
 * Application entry point, this will be rendered the same in all the platforms.
 */
@Composable
fun app(viewModel: SimulationStatusViewModel = viewModel { SimulationStatusViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { topBar(uiState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            controlButton(uiState, viewModel::play, viewModel::pause)
            OutlinedCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(1.dp, Color.Black),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                }
            }
        }
    }
}

/**
 * Top bar put on top of the application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topBar(status: SimulationStatus) {
    TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "Simulation: $status",
            )
        },
    )
}

/**
 * Button to control the simulation.
 */
@Composable
fun controlButton(status: SimulationStatus, resume: () -> Unit, pause: () -> Unit) {
    if (status == SimulationStatus.Running) {
        Button(onClick = { pause() }) {
            Text("Pause", modifier = Modifier.padding(8.dp))
        }
    } else {
        Button(onClick = { resume() }) {
            Text("Resume", modifier = Modifier.padding(8.dp))
        }
    }
}

/**
 * Node graphical representation.
 */
@Composable
fun node(drawScope: DrawScope, id: Int) {
    val x = (drawScope.size.width / 2) + (id * 10)
    val y = (drawScope.size.height / 2) + (id * 10)
    drawScope.drawCircle(Color.White, radius = 10f, center = Offset(x, y))
}
