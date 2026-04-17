/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

private val Midnight = Color(0xFF07111D)
private val DeepSea = Color(0xFF10253B)
private val Ink = Color(0xFF16293C)
private val Panel = Color(0xF0132237)
private val PanelStrong = Color(0xF70D1A2A)
private val Outline = Color(0xFF426988)
private val Accent = Color(0xFFF0B35A)
private val AccentCool = Color(0xFF6AC3FF)
private val Positive = Color(0xFF6ED39C)
private val TextPrimary = Color(0xFFF4F0E8)
private val TextSecondary = Color(0xFFDCE7F2)
private val TextMuted = Color(0xFFC1D0DE)
private val Danger = Color(0xFFD98B8B)
private const val ZoomStep = 1.12f
private const val NodeHitRadius = 22f
private const val SelectedNodeRadius = 18f
private const val SelectedNodeInnerRadius = 12f
private const val NodeRadius = 7f
private const val LinkStrokeWidth = 1.5f
private const val GridVerticalDivisions = 8
private const val GridHorizontalDivisions = 6

/**
 * Main shared screen for the simulator UI.
 */
@Composable
fun AlchemistUiRoot(state: AlchemistUiState, callbacks: AlchemistUiCallbacks) {
    val coroutineScope = rememberCoroutineScope()
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Accent,
            primaryVariant = AccentCool,
            secondary = AccentCool,
            background = Midnight,
            surface = Panel,
            onPrimary = Midnight,
            onSecondary = Midnight,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        ),
        typography = MaterialTheme.typography.copy(
            h4 = MaterialTheme.typography.h4.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
            ),
            h6 = MaterialTheme.typography.h6.copy(
                fontFamily = FontFamily.Serif,
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Midnight, DeepSea, Ink),
                    ),
                ),
        ) {
            val compactLayout = maxWidth < 980.dp
            val inspectorVisible = state.inspector != null
            val inspectorWidth = 324.dp
            val bottomBarHeight = 112.dp
            val layoutSpacing = 20.dp
            if (compactLayout) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(layoutSpacing),
                ) {
                    SimulationPrimaryPane(
                        state = state,
                        callbacks = callbacks,
                        dockWidthFraction = 1f,
                        spacing = layoutSpacing,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (inspectorVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x66050A11))
                                .clickable(onClick = { coroutineScope.launch { callbacks.onInspectorDismiss() } }),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = bottomBarHeight + 12.dp)
                                .fillMaxWidth(),
                        ) {
                            NodeInspector(
                                inspector = requireNotNull(state.inspector),
                                onDismiss = { coroutineScope.launch { callbacks.onInspectorDismiss() } },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(layoutSpacing),
                    horizontalArrangement = Arrangement.spacedBy(layoutSpacing),
                ) {
                    SimulationPrimaryPane(
                        state = state,
                        callbacks = callbacks,
                        dockWidthFraction = 0.84f,
                        spacing = layoutSpacing,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    AnimatedVisibility(
                        visible = inspectorVisible,
                        enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut(),
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(inspectorWidth),
                    ) {
                        state.inspector?.let {
                            NodeInspector(
                                inspector = it,
                                onDismiss = { coroutineScope.launch { callbacks.onInspectorDismiss() } },
                                modifier = Modifier.fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulationPrimaryPane(
    state: AlchemistUiState,
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
            scene = state.scene,
            selectedNodeId = state.selectedNodeId,
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
            ControlDock(
                controls = state.controls,
                modifier = Modifier
                    .fillMaxWidth(dockWidthFraction)
                    .wrapContentHeight(),
                callbacks = callbacks,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ViewportSurface(
    scene: ViewportScene,
    selectedNodeId: Int?,
    callbacks: AlchemistUiCallbacks,
    modifier: Modifier = Modifier,
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var camera by remember { mutableStateOf(ViewportCameraState()) }
    var fixedProjection by remember { mutableStateOf<ViewportProjection?>(null) }
    var middleDragAnchor by remember { mutableStateOf<Offset?>(null) }
    val candidateProjection = remember(scene.nodes, viewportSize) { scene.createViewportProjection(viewportSize) }
    val projection = fixedProjection ?: candidateProjection
    LaunchedEffect(candidateProjection) {
        if (fixedProjection == null && candidateProjection != null) {
            fixedProjection = candidateProjection
        }
    }
    val baseNodes = remember(scene.nodes, viewportSize, projection) { renderNodes(scene, viewportSize, projection) }
    val renderedNodes = remember(baseNodes, viewportSize, camera) {
        baseNodes.map { node ->
            node.copy(center = node.center.toScreenPosition(viewportSize, camera))
        }
    }
    val renderedEdges = remember(scene.edges, renderedNodes) { renderEdges(scene.edges, renderedNodes) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val tapThresholdPx = with(density) { NodeHitRadius.dp.toPx() }
    Surface(
        modifier = modifier,
        color = Panel,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(28.dp),
        elevation = 0.dp,
    ) {
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Outline.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(28.dp),
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(DeepSea.copy(alpha = 0.55f), Midnight),
                        radius = 1600f,
                    ),
                ),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates -> viewportSize = coordinates.size }
                    .pointerInput(renderedNodes, selectedNodeId) {
                        detectTapGestures { tapOffset ->
                            val hit = renderedNodes
                                .minByOrNull { node -> node.center.distanceTo(tapOffset) }
                                ?.takeIf { node -> node.center.distanceTo(tapOffset) <= tapThresholdPx }
                            if (hit != null) {
                                coroutineScope.launch { callbacks.onNodeSelected(hit.node.id) }
                            } else {
                                coroutineScope.launch { callbacks.onInspectorDismiss() }
                            }
                        }
                    }
                    .onPointerEvent(PointerEventType.Press) { event ->
                        val change = event.changes.firstOrNull() ?: return@onPointerEvent
                        middleDragAnchor = if (event.buttons.isTertiaryPressed) {
                            change.position
                        } else {
                            null
                        }
                    }
                    .onPointerEvent(PointerEventType.Move) { event ->
                        val change = event.changes.firstOrNull() ?: return@onPointerEvent
                        if (event.buttons.isTertiaryPressed) {
                            val previous = middleDragAnchor ?: change.position
                            val delta = change.position - previous
                            if (delta != Offset.Zero) {
                                camera = camera.panBy(delta)
                            }
                            middleDragAnchor = change.position
                        } else {
                            middleDragAnchor = null
                        }
                    }
                    .onPointerEvent(PointerEventType.Release) {
                        middleDragAnchor = null
                    }
                    .onPointerEvent(PointerEventType.Scroll) { event ->
                        val pointerChange = event.changes.firstOrNull() ?: return@onPointerEvent
                        val scroll = pointerChange.scrollDelta
                        if (scroll != Offset.Zero && viewportSize.width > 0 && viewportSize.height > 0) {
                            camera = camera.zoomBy(
                                viewportSize = viewportSize,
                                pivot = pointerChange.position,
                                scrollDelta = scroll.y,
                            )
                        }
                    },
            ) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(DeepSea.copy(alpha = 0.55f), Midnight),
                    ),
                )
                drawGrid(size)
                if (scene.showLinks) {
                    renderedEdges.forEach { edge ->
                        drawLine(
                            color = Outline.copy(alpha = 0.42f),
                            start = edge.start,
                            end = edge.end,
                            strokeWidth = LinkStrokeWidth.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                renderedNodes.forEach { rendered ->
                    val isSelected = rendered.node.id == selectedNodeId
                    val nodeColor = lerp(AccentCool, Accent, rendered.node.accent)
                    if (isSelected) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.20f),
                            radius = SelectedNodeRadius.dp.toPx(),
                            center = rendered.center,
                        )
                        drawCircle(
                            color = Accent,
                            radius = SelectedNodeInnerRadius.dp.toPx(),
                            center = rendered.center,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(nodeColor, nodeColor.copy(alpha = 0.45f)),
                            center = rendered.center,
                            radius = SelectedNodeRadius.dp.toPx(),
                        ),
                        radius = NodeRadius.dp.toPx(),
                        center = rendered.center,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Alchemist Simulator",
                    style = MaterialTheme.typography.h4,
                )
                Text(
                    text = scene.message,
                    style = MaterialTheme.typography.body2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                SummaryRail(
                    summary = scene.summary,
                    showLinks = scene.showLinks,
                    onToggleLinks = { coroutineScope.launch { callbacks.onToggleLinks() } },
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                color = PanelStrong.copy(alpha = 0.88f),
                shape = RoundedCornerShape(18.dp),
                elevation = 0.dp,
            ) {
                Text(
                    text = if (scene.nodes.isEmpty()) {
                        "No nodes to display"
                    } else {
                        "Click to inspect · middle-drag to pan · wheel to zoom"
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.caption,
                )
            }
        }
    }
}

@Composable
private fun SummaryRail(summary: List<InfoField>, showLinks: Boolean, onToggleLinks: () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        summary.forEach { item ->
            Surface(
                color = PanelStrong.copy(alpha = 0.82f),
                shape = RoundedCornerShape(999.dp),
                elevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.label.uppercase(),
                        style = MaterialTheme.typography.caption,
                        color = TextSecondary,
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.clickable(onClick = onToggleLinks),
            color = if (showLinks) AccentCool.copy(alpha = 0.2f) else PanelStrong.copy(alpha = 0.82f),
            shape = RoundedCornerShape(999.dp),
            elevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "LINKS",
                    style = MaterialTheme.typography.caption,
                    color = if (showLinks) AccentCool else TextSecondary,
                )
                Text(
                    text = if (showLinks) "ON" else "OFF",
                    style = MaterialTheme.typography.subtitle1,
                )
            }
        }
    }
}

@Composable
private fun ControlDock(
    controls: SimulationControlsState,
    callbacks: AlchemistUiCallbacks,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = modifier,
        color = PanelStrong,
        shape = RoundedCornerShape(28.dp),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransportButton(label = "Play", enabled = controls.canPlay, accent = Positive, onClick = { coroutineScope.launch { callbacks.onPlay() }})
                TransportButton(label = "Pause", enabled = controls.canPause, accent = Danger, onClick = { coroutineScope.launch { callbacks.onPause() }})
                TransportButton(label = "Step", enabled = controls.canStep, accent = Accent, onClick = { coroutineScope.launch { callbacks.onStep() }})
            }
            StatusPill(controls = controls)
            MetricBlock(label = "Time", value = controls.timeLabel)
            MetricBlock(label = "Step", value = controls.step.toString())
            ProgressSection(
                progress = controls.progress,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TransportButton(label: String, enabled: Boolean, accent: Color, onClick: () -> Unit) {
    val buttonContentColor = if (accent.luminance() > 0.35f) Midnight else TextPrimary
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = accent.copy(alpha = if (enabled) 0.92f else 0.28f),
            contentColor = buttonContentColor,
            disabledBackgroundColor = Outline.copy(alpha = 0.65f),
            disabledContentColor = TextSecondary,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = label)
    }
}

@Composable
private fun StatusPill(controls: SimulationControlsState) {
    val color =
        when (controls.status) {
            SimulationStatus.RUNNING -> Positive
            SimulationStatus.PAUSED -> Accent
            SimulationStatus.TERMINATED -> Danger
            else -> AccentCool
        }
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = color, shape = CircleShape),
            )
            Text(
                text = controls.statusLabel,
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}

@Composable
private fun MetricBlock(label: String, value: String) {
    Surface(
        color = Panel.copy(alpha = 0.78f),
        shape = RoundedCornerShape(18.dp),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.caption,
                color = AccentCool,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily.Monospace),
            )
        }
    }
}

@Composable
private fun ProgressSection(progress: SimulationProgress, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Simulation progress",
                style = MaterialTheme.typography.subtitle1,
            )
            Text(
                text = progress.label,
                style = MaterialTheme.typography.caption,
                color = TextSecondary,
            )
        }
        if (progress.fraction == null) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Accent,
                backgroundColor = Outline.copy(alpha = 0.55f),
            )
        } else {
            LinearProgressIndicator(
                progress = progress.fraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Accent,
                backgroundColor = Outline.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun NodeInspector(inspector: NodeInspectorState, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = PanelStrong,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(28.dp),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = inspector.title,
                        style = MaterialTheme.typography.h6,
                    )
                    Text(
                        text = inspector.subtitle,
                        style = MaterialTheme.typography.body2,
                        color = TextSecondary,
                    )
                }
                TransportButton(
                    label = "Close",
                    enabled = true,
                    accent = Outline,
                    onClick = onDismiss,
                )
            }
            InspectorSection(
                title = "Position",
                description = "Coordinates in simulator space.",
                fields = inspector.position,
            )
            InspectorSection(
                title = "Concentrations",
                description = "Live contents currently stored in the selected node.",
                fields = inspector.concentrations.ifEmpty {
                    listOf(InfoField("No molecules", "This node exposes no concentrations"))
                },
            )
            InspectorSection(
                title = "Metadata",
                description = "Simulator-provided details exposed by the current adapter.",
                fields = inspector.metadata.ifEmpty {
                    listOf(InfoField("Unavailable", "No extra metadata available"))
                },
            )
        }
    }
}

@Composable
private fun InspectorSection(title: String, description: String, fields: List<InfoField>) {
    Surface(
        color = Panel.copy(alpha = 0.72f),
        contentColor = TextPrimary,
        shape = RoundedCornerShape(22.dp),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                )
            }
            fields.forEachIndexed { index, field ->
                if (index > 0) {
                    Divider(color = Outline.copy(alpha = 0.42f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = field.label,
                        style = MaterialTheme.typography.body2,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = field.value,
                        style = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily.Monospace),
                    )
                }
            }
        }
    }
}

private fun renderNodes(
    scene: ViewportScene,
    viewportSize: IntSize,
    projection: ViewportProjection?,
): List<RenderedNode> {
    if (projection == null || scene.nodes.isEmpty() || viewportSize.width == 0 || viewportSize.height == 0) {
        return emptyList()
    }
    return scene.nodes.map { node ->
        RenderedNode(node = node, center = node.toViewportPosition(viewportSize, projection))
    }
}

private fun renderEdges(edges: List<ViewportEdge>, renderedNodes: List<RenderedNode>): List<RenderedEdge> {
    val nodesById = renderedNodes.associateBy { it.node.id }
    return edges.mapNotNull { edge ->
        val from = nodesById[edge.fromNodeId] ?: return@mapNotNull null
        val to = nodesById[edge.toNodeId] ?: return@mapNotNull null
        RenderedEdge(start = from.center, end = to.center)
    }
}

private data class RenderedNode(val node: ViewportNode, val center: Offset)

private data class RenderedEdge(val start: Offset, val end: Offset)

private data class ViewportCameraState(
    val pan: Offset = Offset.Zero,
    val zoom: Float = 1f,
)

private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun Offset.toScreenPosition(
    viewportSize: IntSize,
    camera: ViewportCameraState,
): Offset {
    val center = viewportSize.center
    return center + ((this - center) * camera.zoom) + camera.pan
}

private fun ViewportCameraState.panBy(delta: Offset): ViewportCameraState = copy(pan = pan + delta)

private fun ViewportCameraState.zoomBy(
    viewportSize: IntSize,
    pivot: Offset,
    scrollDelta: Float,
): ViewportCameraState {
    val zoomFactor = when {
        scrollDelta < 0f -> ZoomStep
        scrollDelta > 0f -> 1f / ZoomStep
        else -> 1f
    }
    val targetZoom = applyInfiniteZoomFactor(zoom, zoomFactor)
    if (targetZoom == zoom) {
        return this
    }
    val center = viewportSize.center
    val worldPoint = pivot.toWorldPosition(viewportSize, this)
    val newPan = pivot - center - ((worldPoint - center) * targetZoom)
    return copy(zoom = targetZoom, pan = newPan)
}

internal fun applyInfiniteZoomFactor(currentZoom: Float, zoomFactor: Float): Float {
    if (currentZoom <= 0f || !currentZoom.isFinite() || zoomFactor <= 0f || !zoomFactor.isFinite()) {
        return currentZoom
    }
    val targetZoom = currentZoom * zoomFactor
    return when {
        targetZoom.isNaN() -> currentZoom
        targetZoom == 0f -> Float.MIN_VALUE
        targetZoom == Float.POSITIVE_INFINITY -> Float.MAX_VALUE
        else -> targetZoom
    }
}

private fun Offset.toWorldPosition(
    viewportSize: IntSize,
    camera: ViewportCameraState,
): Offset {
    val center = viewportSize.center
    return center + ((this - center - camera.pan) / camera.zoom)
}

private val IntSize.center: Offset
    get() = Offset(width / 2f, height / 2f)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(canvasSize: Size) {
    val stepX = canvasSize.width / GridVerticalDivisions.toFloat()
    val stepY = canvasSize.height / GridHorizontalDivisions.toFloat()
    for (column in 1 until GridVerticalDivisions) {
        drawLine(
            color = Outline.copy(alpha = 0.32f),
            start = Offset(stepX * column, 0f),
            end = Offset(stepX * column, canvasSize.height),
            strokeWidth = 1f,
        )
    }
    for (row in 1 until GridHorizontalDivisions) {
        drawLine(
            color = Outline.copy(alpha = 0.28f),
            start = Offset(0f, stepY * row),
            end = Offset(canvasSize.width, stepY * row),
            strokeWidth = 1f,
        )
    }
    drawLine(
        color = Outline.copy(alpha = 0.7f),
        start = Offset(canvasSize.width / 2f, 0f),
        end = Offset(canvasSize.width / 2f, canvasSize.height),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Outline.copy(alpha = 0.7f),
        start = Offset(0f, canvasSize.height / 2f),
        end = Offset(canvasSize.width, canvasSize.height / 2f),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
    )
}

private fun lerp(start: Color, end: Color, amount: Float): Color {
    val clamped = min(1f, max(0f, amount))
    return Color(
        red = start.red + (end.red - start.red) * clamped,
        green = start.green + (end.green - start.green) * clamped,
        blue = start.blue + (end.blue - start.blue) * clamped,
        alpha = start.alpha + (end.alpha - start.alpha) * clamped,
    )
}
