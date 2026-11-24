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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.view.inspector.InspectorActions
import it.unibo.alchemist.boundary.composeui.view.inspector.ResizableInspector
import it.unibo.alchemist.boundary.composeui.view.inspector.ValueEditor
import it.unibo.alchemist.boundary.composeui.view.inspector.shownInspectorWidth
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
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
private const val EDITOR_SHARE = 0.6f

private val defaultInspectorWidth = 324.dp

/** The help button sits closer to the corner than the other panels. */
private val helpMargin = 8.dp

/**
 * Main shared screen for the simulator UI: the viewport fills the window, and the other controls float over it.
 */
@Composable
internal fun AlchemistUiRoot(state: AlchemistUiState, controller: ComposeUiController) {
    val coroutineScope = rememberCoroutineScope()
    val transport = controller.callbacks.transport
    val nodes = controller.callbacks.nodes
    // Frames carry reactions only for the nodes selected when taken: ask for those of newly selected nodes.
    LaunchedEffect(state.selectedNodeIds) {
        state.scene.uninspected(state.selectedNodeIds)
            .takeIf { it.isNotEmpty() }
            ?.let { controller.callbacks.display.inspect(it) }
    }
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
                        onDismissRequest = controller.edits.dialogs::dismiss,
                        title = { Text(dialog.title) },
                        text = { Text(dialog.message) },
                        confirmButton = {
                            androidx.compose.material.TextButton(
                                onClick = controller.edits.dialogs::dismiss,
                            ) {
                                Text("OK")
                            }
                        },
                        backgroundColor = SurfaceStrong,
                        contentColor = TextPrimary,
                    )
                }
                // The value editor is outside the Space shortcut, so that its text can have spaces.
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    var inspectorWidth by remember { mutableStateOf(defaultInspectorWidth) }
                    val inspectorRoom = maxWidth - OverlayMargin * 2
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .playPauseOnSpace(
                                controls = state.controls,
                                onPlay = { coroutineScope.launch { transport.play() } },
                                onPause = { coroutineScope.launch { transport.pause() } },
                            ),
                    ) {
                        var displayedInspector by remember { mutableStateOf(state.inspector) }
                        state.inspector?.let { displayedInspector = it }
                        var dockVisible by remember { mutableStateOf(true) }
                        var helpVisible by remember { mutableStateOf(false) }
                        ViewportSurface(
                            scene = state.scene,
                            selectedNodeIds = state.selectedNodeIds,
                            controller = controller,
                            onToggleHelp = { helpVisible = !helpVisible },
                            onToggleControls = { dockVisible = !dockVisible },
                            modifier = Modifier.fillMaxSize(),
                        )
                        Row(modifier = Modifier.fillMaxSize()) {
                            ControlsOverlay(
                                controls = state.controls,
                                nodeCount = state.scene.nodes.size,
                                showLinks = state.scene.showLinks,
                                controller = controller,
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
                                displayedInspector?.let { inspector ->
                                    ResizableInspector(
                                        inspector = inspector,
                                        width = inspectorWidth,
                                        availableWidth = inspectorRoom,
                                        onWidthChange = { width -> inspectorWidth = width },
                                        actions = InspectorActions(
                                            onDismiss = controller.edits.selection::clear,
                                            onConcentrationSet = { nodeIds, molecule, value ->
                                                coroutineScope.launch {
                                                    nodes.setConcentration(nodeIds, molecule, value).onFailure {
                                                        controller.edits.dialogs.show(
                                                            "Invalid edit",
                                                            it.refusalMessage(),
                                                        )
                                                    }
                                                }
                                            },
                                            onMoleculeRemoved = { nodeIds, molecule ->
                                                coroutineScope.launch { nodes.removeMolecule(nodeIds, molecule) }
                                            },
                                            onValueEditorToggled = controller.edits.selection::toggleValueEditor,
                                        ),
                                        modifier = Modifier
                                            .padding(OverlayMargin)
                                            .fillMaxHeight(),
                                    )
                                }
                            }
                        }
                    }
                    state.valueEditor?.let { editor ->
                        val viewportFocus = LocalViewportFocus.current
                        // The editor sticks to the left of the inspector, so it shrinks as the inspector widens.
                        val inspectorSpace = when (state.inspector) {
                            null -> OverlayMargin
                            else -> shownInspectorWidth(inspectorWidth, inspectorRoom) + OverlayMargin
                        }
                        Box(modifier = Modifier.fillMaxSize().padding(end = inspectorSpace)) {
                            ValueEditor(
                                editor = editor,
                                onEdit = { text -> nodes.setConcentration(editor.nodeIds, editor.molecule, text) },
                                onClose = {
                                    controller.edits.selection.closeValueEditor()
                                    viewportFocus.requestFocus()
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxWidth(EDITOR_SHARE)
                                    .fillMaxHeight(EDITOR_SHARE),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Throwable.refusalMessage(): String = message ?: "The simulation refused the change: $this"
