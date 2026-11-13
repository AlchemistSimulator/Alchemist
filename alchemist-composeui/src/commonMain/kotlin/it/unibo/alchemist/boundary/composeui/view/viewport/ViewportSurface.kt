/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.theme.Background
import it.unibo.alchemist.boundary.composeui.view.theme.BackgroundVariant
import it.unibo.alchemist.boundary.composeui.view.theme.NODE_HIT_RADIUS
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.Surface as SurfaceColor
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import kotlinx.coroutines.launch

private val dragThreshold = 6.dp

/**
 * The simulation viewport: draws the scene and handles camera, selection and node dragging.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ViewportSurface(
    scene: ViewportScene,
    selectedNodeIds: List<Int>,
    callbacks: AlchemistUiCallbacks,
    modifier: Modifier = Modifier,
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var camera by remember { mutableStateOf(ViewportCameraState()) }
    var fixedProjection by remember { mutableStateOf<ViewportProjection?>(null) }
    var drag by remember { mutableStateOf<ViewportDrag>(ViewportDrag.None) }
    val candidateProjection = remember(scene.nodes, viewportSize) { scene.createViewportProjection(viewportSize) }
    val projection = fixedProjection ?: candidateProjection
    LaunchedEffect(candidateProjection) {
        if (fixedProjection == null && candidateProjection != null) {
            fixedProjection = candidateProjection
        }
    }
    val sceneCache = remember(scene, viewportSize, projection) {
        buildViewportSceneCache(scene, viewportSize, projection)
    }
    val viewportFrame = remember(sceneCache, viewportSize, camera) {
        buildViewportFrame(sceneCache, viewportSize, camera)
    }
    val selectedNodeIdSet = remember(selectedNodeIds) { selectedNodeIds.toHashSet() }
    val density = LocalDensity.current
    val dragThresholdPx = with(density) { dragThreshold.toPx() }
    val gestures = ViewportGestures(
        sceneCache = sceneCache,
        viewportSize = viewportSize,
        camera = camera,
        projection = projection,
        selectedNodeIds = selectedNodeIdSet,
        tapThresholdPx = with(density) { NODE_HIT_RADIUS.dp.toPx() },
        dragThresholdPx = dragThresholdPx,
    )
    val selectionRect = (drag as? ViewportDrag.Select)?.takeIf { it.length >= dragThresholdPx }?.rect
    val coroutineScope = rememberCoroutineScope()
    val send: (ViewportAction) -> Unit = { action -> coroutineScope.launch { callbacks.action() } }
    val selectFirstNode = { scene.nodes.firstOrNull()?.also { node -> send { onNodeSelected(node.id) } } != null }
    val clearSelection = { selectedNodeIds.isNotEmpty().also { if (it) send { onInspectorDismiss() } } }
    val pan = { delta: Offset -> camera = camera.panBy(delta) }
    Surface(
        modifier = modifier,
        color = SurfaceColor,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 1.dp, color = Outline.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(BackgroundVariant.copy(alpha = 0.55f), Background),
                        radius = 1600f,
                    ),
                ),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates -> viewportSize = coordinates.size }
                    .focusable()
                    .viewportSemantics(
                        scene = scene,
                        selectedNodeCount = selectedNodeIds.size,
                        zoom = camera.zoom,
                        onSelectFirstNode = selectFirstNode,
                        onClearSelection = clearSelection,
                        onZoomIn = { camera = camera.zoomIn(viewportSize) },
                        onZoomOut = { camera = camera.zoomOut(viewportSize) },
                        onPan = pan,
                    )
                    .viewportKeyboard(
                        onPan = pan,
                        onSelectFirstNode = selectFirstNode,
                        onClearSelection = clearSelection,
                    )
                    .onPointerEvent(PointerEventType.Press) { event ->
                        event.changes.firstOrNull()?.let { change ->
                            drag = gestures.press(
                                position = change.position,
                                secondaryPressed = event.buttons.isSecondaryPressed,
                                ctrlPressed = event.keyboardModifiers.isCtrlPressed,
                            )
                        }
                    }
                    .onPointerEvent(PointerEventType.Move) { event ->
                        event.changes.firstOrNull()?.let { change ->
                            val secondaryPressed = event.buttons.isSecondaryPressed
                            val lastPan = (drag as? ViewportDrag.Pan)?.last
                            if (secondaryPressed && lastPan != null) {
                                pan(change.position - lastPan)
                            }
                            drag = drag.movedTo(change.position, secondaryPressed)
                        }
                    }
                    .onPointerEvent(PointerEventType.Release) {
                        gestures.release(drag)?.let(send)
                        drag = ViewportDrag.None
                    }
                    .onPointerEvent(PointerEventType.Scroll) { event ->
                        event.changes.firstOrNull()
                            ?.takeIf { it.scrollDelta != Offset.Zero }
                            ?.let { change ->
                                camera =
                                    camera.zoomBy(viewportSize, change.position, change.scrollDelta.y)
                            }
                    },
            ) {
                drawBackdrop()
                drawGrid(camera)
                drawScene(scene, viewportFrame, camera, selectedNodeIdSet, drag)
                selectionRect?.let { drawSelection(it) }
            }
            ViewportChrome(
                scene = scene,
                onToggleLinks = { send { onToggleLinks() } },
                modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
            )
            ViewportGestureHint(
                hasNodes = scene.nodes.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
            )
            ViewportGridLegend(
                viewportSize = viewportSize,
                zoom = camera.zoom,
                projection = projection,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            )
        }
    }
}
