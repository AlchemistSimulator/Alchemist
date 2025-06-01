/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.unibo.alchemist.boundary.composeui.viewmodels.NodeViewModel

/**
 * Display the information of a node, subscribing to its own channel for data.
 */
@Composable
fun NodeDrawer(nodeId: Int) {
    val nodeModel: NodeViewModel = viewModel(key = "node-$nodeId") { NodeViewModel(nodeId) }
    val nodeInfo by nodeModel.nodeInfo.collectAsStateWithLifecycle()
    Text("Node $nodeId: $nodeInfo")
}
