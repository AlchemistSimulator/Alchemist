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
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.inspector.NodeInspector
import it.unibo.alchemist.boundary.composeui.view.theme.Background
import it.unibo.alchemist.boundary.composeui.view.theme.OverlayMargin
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.Surface
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary
import it.unibo.alchemist.boundary.composeui.view.viewport.LocalViewportFocus
import it.unibo.alchemist.boundary.composeui.view.viewport.ViewportSurface
import kotlinx.coroutines.launch

private const val INSPECTOR_SLIDE_MILLIS = 320
private const val INSPECTOR_FADE_MILLIS = 200

private val inspectorWidth = 324.dp

/** The help button sits closer to the corner than the other panels. */
private val helpMargin = 8.dp

/**
 * Main shared screen for the simulator UI: the viewport fills the window, and the other controls float over it.
 */
@Composable
internal fun AlchemistUiRoot(state: AlchemistUiState, callbacks: AlchemistUiCallbacks) {
    val coroutineScope = rememberCoroutineScope()
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = PrimaryAccent,
            primaryVariant = SecondaryAccent,
            secondary = SecondaryAccent,
            background = Background,
            surface = Surface,
            onPrimary = Background,
            onSecondary = Background,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        ),
        typography = MaterialTheme.typography.copy(
            h4 = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            h6 = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            ),
            body2 = MaterialTheme.typography.body2.copy(
                color = TextSecondary,
            ),
            caption = MaterialTheme.typography.caption.copy(
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
            ),
            button = MaterialTheme.typography.button.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        ),
    ) {
        ScaledToWindow(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalViewportFocus provides remember { FocusRequester() }) {
                state.controls.dialog?.let { dialog ->
                    androidx.compose.material.AlertDialog(
                        onDismissRequest = { coroutineScope.launch { callbacks.onDialogDismiss() } },
                        title = { Text(dialog.title) },
                        text = { Text(dialog.message) },
                        confirmButton = {
                            androidx.compose.material.TextButton(
                                onClick = { coroutineScope.launch { callbacks.onDialogDismiss() } },
                            ) {
                                Text("OK")
                            }
                        },
                        backgroundColor = SurfaceStrong,
                        contentColor = TextPrimary,
                    )
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .playPauseOnSpace(
                            controls = state.controls,
                            onPlay = { coroutineScope.launch { callbacks.onPlay() } },
                            onPause = { coroutineScope.launch { callbacks.onPause() } },
                        ),
                ) {
                    var displayedInspector by remember { mutableStateOf(state.inspector) }
                    state.inspector?.let { displayedInspector = it }
                    var dockVisible by remember { mutableStateOf(true) }
                    var helpVisible by remember { mutableStateOf(false) }
                    ViewportSurface(
                        scene = state.scene,
                        selectedNodeIds = state.selectedNodeIds,
                        callbacks = callbacks,
                        onToggleHelp = { helpVisible = !helpVisible },
                        onToggleControls = { dockVisible = !dockVisible },
                        modifier = Modifier.fillMaxSize(),
                    )
                    Row(modifier = Modifier.fillMaxSize()) {
                        ControlsOverlay(
                            controls = state.controls,
                            nodeCount = state.scene.nodes.size,
                            callbacks = callbacks,
                            dockVisible = dockVisible,
                            onToggleDock = { dockVisible = !dockVisible },
                            helpVisible = helpVisible,
                            onToggleHelp = { helpVisible = !helpVisible },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(helpMargin),
                        )
                        AnimatedVisibility(
                            visible = state.inspector != null,
                            enter = expandHorizontally(
                                tween(INSPECTOR_SLIDE_MILLIS),
                            ) + fadeIn(tween(INSPECTOR_FADE_MILLIS)),
                            exit = shrinkHorizontally(
                                tween(INSPECTOR_SLIDE_MILLIS),
                            ) + fadeOut(tween(INSPECTOR_FADE_MILLIS)),
                        ) {
                            displayedInspector?.let {
                                NodeInspector(
                                    inspector = it,
                                    onDismiss = { coroutineScope.launch { callbacks.onInspectorDismiss() } },
                                    modifier = Modifier
                                        .padding(OverlayMargin)
                                        .width(
                                            minOf(inspectorWidth, this@BoxWithConstraints.maxWidth - OverlayMargin * 2),
                                        )
                                        .fillMaxHeight(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
