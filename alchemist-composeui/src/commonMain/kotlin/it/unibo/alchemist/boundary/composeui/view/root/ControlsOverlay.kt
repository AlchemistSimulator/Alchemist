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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.controls.ControlDock
import it.unibo.alchemist.boundary.composeui.view.controls.SettingsPanel
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary
import it.unibo.alchemist.boundary.composeui.view.viewport.HelpButton
import it.unibo.alchemist.boundary.composeui.view.viewport.HelpButtonSize
import it.unibo.alchemist.boundary.composeui.view.viewport.ViewportGestureHint
import it.unibo.alchemist.boundary.composeui.view.viewport.clickableKeepingShortcuts
import kotlinx.coroutines.launch

private const val HANDLE_ALPHA = 0.6f

private val handleWidth = 96.dp
private val handleTouchHeight = 16.dp
private val handleThickness = 4.dp
private val settingsGap = 8.dp

/**
 * The controls at the bottom of the window: the help, and the control dock.
 * The help panels open above, so they never cover the dock.
 * A handle line above the dock hides or shows it, and the ⚙ button of the dock opens the settings above it.
 */
@Composable
internal fun ControlsOverlay(
    controls: SimulationControlsState,
    nodeCount: Int,
    showLinks: Boolean,
    controller: ComposeUiController,
    dockVisible: Boolean,
    onToggleDock: () -> Unit,
    helpVisible: Boolean,
    onToggleHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val edits = controller.edits
    val transport = controller.callbacks.transport
    val pace = controller.callbacks.pace
    val send: (suspend () -> Unit) -> Unit = { command -> coroutineScope.launch { command() } }
    var settingsOpen by remember { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)) {
        ViewportGestureHint(hasNodes = nodeCount > 0, expanded = helpVisible)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            HelpButton(expanded = helpVisible, onToggle = onToggleHelp)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                // As wide as the dock, so the settings panel lines up with its right end.
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(
                        visible = dockVisible && settingsOpen,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SettingsPanel(
                            controls = controls,
                            showLinks = showLinks,
                            onFpsInputChanged = edits.frameRate::setInput,
                            onFpsSubmit = edits.frameRate::apply,
                            onToggleLinks = { send(controller.callbacks.display::toggleLinks) },
                            modifier = Modifier.padding(bottom = settingsGap),
                        )
                    }
                    DockHandle(
                        dockVisible = dockVisible,
                        onToggle = onToggleDock,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    AnimatedVisibility(
                        visible = dockVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        ControlDock(
                            controls = controls,
                            nodeCount = nodeCount,
                            settingsOpen = settingsOpen,
                            onPlay = { send(transport::play) },
                            onPause = { send(transport::pause) },
                            onStep = { send(transport::step) },
                            onToTimeInputChanged = edits.jumps::setTimeInput,
                            onToTimeSubmit = { edits.jumps.timeTarget()?.let { send { transport.runToTime(it) } } },
                            onToStepInputChanged = edits.jumps::setStepInput,
                            onToStepSubmit = { edits.jumps.stepTarget()?.let { send { transport.runToStep(it) } } },
                            onJumpTimeBy = { send { transport.jumpTimeBy(it) } },
                            onJumpStepBy = { send { transport.jumpStepBy(it) } },
                            onPacingInputChanged = { send { pace.setRateInput(it) } },
                            onPacingModeChanged = { send { pace.setMode(it) } },
                            onToggleSettings = { settingsOpen = !settingsOpen },
                        )
                    }
                }
            }
            // Balances the help button, so the dock stays centered.
            Spacer(modifier = Modifier.width(HelpButtonSize))
        }
    }
}

@Composable
private fun DockHandle(dockVisible: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = handleWidth, height = handleTouchHeight)
            .clickableKeepingShortcuts(
                onClickLabel = if (dockVisible) "Hide controls (C)" else "Show controls (C)",
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
