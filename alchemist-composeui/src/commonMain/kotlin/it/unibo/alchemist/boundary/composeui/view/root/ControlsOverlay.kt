/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.controls.ControlDock
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary
import it.unibo.alchemist.boundary.composeui.view.viewport.HelpButton
import it.unibo.alchemist.boundary.composeui.view.viewport.HelpButtonSize
import it.unibo.alchemist.boundary.composeui.view.viewport.ViewportGestureHint
import kotlinx.coroutines.launch

private const val HANDLE_ALPHA = 0.6f

private val handleWidth = 96.dp
private val handleTouchHeight = 16.dp
private val handleThickness = 4.dp

/**
 * The controls at the bottom of the window: the help, and the control dock.
 * The help panels open above, so they never cover the dock.
 * A handle line above the dock hides or shows it.
 */
@Composable
internal fun ControlsOverlay(
    controls: SimulationControlsState,
    hasNodes: Boolean,
    callbacks: AlchemistUiCallbacks,
    dockVisible: Boolean,
    onToggleDock: () -> Unit,
    helpVisible: Boolean,
    onToggleHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)) {
        ViewportGestureHint(hasNodes = hasNodes, expanded = helpVisible)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            HelpButton(expanded = helpVisible, onToggle = onToggleHelp)
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                DockHandle(dockVisible = dockVisible, onToggle = onToggleDock)
                AnimatedVisibility(
                    visible = dockVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
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
                    )
                }
            }
            // Balances the help button, so the dock stays centered.
            Spacer(modifier = Modifier.width(HelpButtonSize))
        }
    }
}

@Composable
private fun DockHandle(dockVisible: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = handleWidth, height = handleTouchHeight)
            .clickable(
                onClickLabel = if (dockVisible) "Hide controls (C)" else "Show controls (C)",
                role = Role.Button,
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(handleThickness)
                .background(TextSecondary.copy(alpha = HANDLE_ALPHA), RoundedCornerShape(percent = 50)),
        )
    }
}
