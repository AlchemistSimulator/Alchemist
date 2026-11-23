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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import it.unibo.alchemist.boundary.composeui.view.theme.Background
import it.unibo.alchemist.boundary.composeui.view.theme.BackgroundVariant
import it.unibo.alchemist.boundary.composeui.view.theme.NODE_HIT_RADIUS
import it.unibo.alchemist.boundary.composeui.view.theme.OverlayMargin
import it.unibo.alchemist.boundary.composeui.view.theme.SELECTED_NODE_RADIUS
import it.unibo.alchemist.boundary.composeui.view.theme.Surface as SurfaceColor
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import kotlinx.coroutines.launch

private const val KEYBOARD_PAN_SPEED_PX_PER_SECOND = 900f

private val dragThreshold = 6.dp
private val actionButtonWidth = 56.dp
private val actionButtonHeight = 24.dp
private val actionGap = 6.dp

/** How far Ctrl+D puts the copies from the originals, down and right. */
private val cloneShift = 24.dp

/**
 * The simulation viewport: draws the scene and handles camera, selection and node dragging.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ViewportSurface(
    scene: ViewportScene,
    selectedNodeIds: List<Int>,
    controller: ComposeUiController,
    onToggleHelp: () -> Unit,
    onToggleControls: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var camera by remember { mutableStateOf(ViewportCameraState()) }
    var fixedProjection by remember { mutableStateOf<ViewportProjection?>(null) }
    var drag by remember { mutableStateOf<ViewportDrag>(ViewportDrag.None) }
    var pointer by remember { mutableStateOf<Offset?>(null) }
    var heldPans by remember { mutableStateOf(emptySet<PanDirection>()) }
    val focusRequester = LocalViewportFocus.current
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
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
    val selectionActions = remember(scene, viewportFrame, selectedNodeIdSet, density) {
        with(density) {
            SelectionActions.of(
                nodeCenters = scene.nodes.indices
                    .filter { scene.nodes[it].id in selectedNodeIdSet }
                    .mapNotNull(viewportFrame.screenPositions::getOrNull),
                margin = SELECTED_NODE_RADIUS.dp.toPx() * camera.zoom,
                buttonSize = Size(actionButtonWidth.toPx(), actionButtonHeight.toPx()),
                gap = actionGap.toPx(),
            )
        }
    }
    // The buttons show while the pointer is over the selection, and stay while Delete is held.
    val shownActions = selectionActions?.takeIf { actions ->
        (drag == ViewportDrag.None || drag is ViewportDrag.DeleteNodes) &&
            pointer?.let(actions.hoverArea::contains) == true
    }
    val gestures = ViewportGestures(
        sceneCache = sceneCache,
        viewportSize = viewportSize,
        camera = camera,
        projection = projection,
        selectedNodeIds = selectedNodeIdSet,
        tapThresholdPx = with(density) { NODE_HIT_RADIUS.dp.toPx() },
        dragThresholdPx = dragThresholdPx,
        selectionActions = shownActions,
    )
    val selectionRect = (drag as? ViewportDrag.Select)?.takeIf { it.length >= dragThresholdPx }?.rect
    val coroutineScope = rememberCoroutineScope()
    val send: (ViewportAction) -> Unit = { action -> coroutineScope.launch { controller.action() } }
    val clearSelection = { selectedNodeIds.isNotEmpty().also { if (it) send { edits.selection.clear() } } }
    val selectNearestNode = {
        pointer?.let(gestures::nearestNode)?.also { node -> send { edits.selection.select(listOf(node.id)) } } != null
    }
    val pan = { delta: Offset -> camera = camera.panBy(delta) }
    LaunchedEffect(heldPans) { panWhileHeld(heldPans, KEYBOARD_PAN_SPEED_PX_PER_SECOND, pan) }
    Surface(
        modifier = modifier,
        color = SurfaceColor,
        contentColor = TextPrimary,
        elevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    .focusRequester(focusRequester)
                    .onFocusChanged { focus -> if (!focus.isFocused) heldPans = emptySet() }
                    .focusable()
                    .viewportSemantics(
                        scene = scene,
                        selectedNodeCount = selectedNodeIds.size,
                        zoom = camera.zoom,
                        onClearSelection = clearSelection,
                        onZoomIn = { camera = camera.zoomIn(viewportSize) },
                        onZoomOut = { camera = camera.zoomOut(viewportSize) },
                        onPan = pan,
                    )
                    .viewportKeyboard(
                        onPanKey = { direction, pressed ->
                            heldPans = if (pressed) heldPans + direction else heldPans - direction
                        },
                        onSelectNearestNode = selectNearestNode,
                        onClearSelection = clearSelection,
                        onDeleteSelection = { gestures.deleteSelection()?.also(send) != null },
                        onCloneSelection = {
                            val shift = with(density) { cloneShift.toPx() }
                            gestures.cloneSelection(Offset(shift, shift))?.also(send) != null
                        },
                        onToggleHelp = onToggleHelp,
                        onToggleControls = onToggleControls,
                    )
                    .onPointerEvent(PointerEventType.Press) { event ->
                        focusRequester.requestFocus()
                        event.changes.firstOrNull()?.let { change ->
                            drag = gestures.press(
                                position = change.position,
                                secondaryPressed = event.buttons.isSecondaryPressed,
                                ctrlPressed = event.keyboardModifiers.isCtrlPressed,
                                shiftPressed = event.keyboardModifiers.isShiftPressed,
                            )
                        }
                    }
                    .onPointerEvent(PointerEventType.Move) { event ->
                        event.changes.firstOrNull()?.let { change ->
                            pointer = change.position
                            val secondaryPressed = event.buttons.isSecondaryPressed
                            val lastPan = (drag as? ViewportDrag.Pan)?.last
                            if (secondaryPressed && lastPan != null) {
                                pan(change.position - lastPan)
                            }
                            drag = drag.movedTo(change.position, secondaryPressed)
                        }
                    }
                    .onPointerEvent(PointerEventType.Exit) { pointer = null }
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
                drawScene(scene, viewportFrame, camera, selectedNodeIdSet, drag, viewportSize)
                selectionRect?.let { drawSelection(it) }
            }
            shownActions?.let { SelectionActionsOverlay(it) }
            ViewportChrome(
                scene = scene,
                modifier = Modifier.align(Alignment.TopStart).padding(OverlayMargin),
            )
            ViewportGridLegend(
                viewportSize = viewportSize,
                zoom = camera.zoom,
                projection = projection,
                modifier = Modifier.align(Alignment.BottomEnd).padding(OverlayMargin),
            )
        }
    }
}
