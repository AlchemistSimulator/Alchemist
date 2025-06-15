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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apollographql.apollo3.api.Error
import it.unibo.alchemist.boundary.composeui.viewmodels.SimulationStatus
import it.unibo.alchemist.boundary.composeui.viewmodels.SimulationViewModel

/**
 * Application entry point, this will be rendered the same in all the platforms.
 */
@Composable
fun App(viewModel: SimulationViewModel = viewModel { SimulationViewModel() }) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()
    val nodes by viewModel.nodes.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopBar(status) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ControlButton(status, viewModel::play, viewModel::pause)
            OutlinedCard(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(1.dp, Color.Black),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.verticalScroll(
                        rememberScrollState(),
                    ),
                ) {
                    if (errors.isNotEmpty()) {
                        ErrorDialog(viewModel::fetch, errors)
                    }
                    for (node in nodes) {
                        NodeDrawer(node.id)
                    }
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
fun TopBar(status: SimulationStatus) {
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
fun ControlButton(status: SimulationStatus, resume: () -> Unit, pause: () -> Unit) {
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
 * Display the error dialog, currently used to circumvent the null issue we're facing when subscribing to simulation.
 */
@Composable
fun ErrorDialog(dismiss: () -> Unit, errors: List<Error>) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Error") },
        text = {
            for (error in errors) {
                Text(error.message)
            }
        },
        confirmButton = {
            Button(onClick = dismiss) {
                Text("OK")
            }
        },
    )
}
