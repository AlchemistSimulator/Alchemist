/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.view.controls.ControlDock
import it.unibo.alchemist.boundary.composeui.view.viewport.ViewportSurface
import kotlinx.coroutines.launch

@Composable
internal fun SimulationPrimaryPane(
    scene: ViewportScene,
    controls: SimulationControlsState,
    selectedNodeIds: List<Int>,
    callbacks: AlchemistUiCallbacks,
    dockWidthFraction: Float,
    spacing: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        ViewportSurface(
            scene = scene,
            selectedNodeIds = selectedNodeIds,
            callbacks = callbacks,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val coroutineScope = rememberCoroutineScope()
            Box(
                modifier = Modifier.fillMaxWidth(dockWidthFraction),
                contentAlignment = Alignment.Center,
            ) {
                ControlDock(
                    controls = controls,
                    onPlay = { coroutineScope.launch { callbacks.onPlay() } },
                    onPause = { coroutineScope.launch { callbacks.onPause() } },
                    onStep = { coroutineScope.launch { callbacks.onStep() } },
                    onToTimeInputChanged = { coroutineScope.launch { callbacks.onToTimeInputChanged(it) } },
                    onToTimeSubmit = { coroutineScope.launch { callbacks.onToTimeSubmit() } },
                    onToStepInputChanged = { coroutineScope.launch { callbacks.onToStepInputChanged(it) } },
                    onToStepSubmit = { coroutineScope.launch { callbacks.onToStepSubmit() } },
                    onFpsInputChanged = { coroutineScope.launch { callbacks.onFpsInputChanged(it) } },
                    onFpsSubmit = { coroutineScope.launch { callbacks.onFpsSubmit() } },
                    onEventRateChanged = { coroutineScope.launch { callbacks.onEventRateChanged(it) } },
                    modifier = Modifier.wrapContentHeight(),
                )
            }
        }
    }
}
