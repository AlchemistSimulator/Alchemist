/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("MagicNumber", "UndocumentedPublicProperty")

/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
import it.unibo.alchemist.boundary.composeui.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.ControlDialogState
import it.unibo.alchemist.boundary.composeui.model.InspectorState
import it.unibo.alchemist.boundary.composeui.model.InspectorState.NodeGroup
import it.unibo.alchemist.boundary.composeui.model.InspectorState.SingleNode
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationProgress
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.util.ViewportNodes.toWorldBounds
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the UI state; safe to use from any thread.
 * The UI reads [stateFlow]; all changes go through [set] or [update].
 */
class ComposeUiStateStore(initialState: AlchemistUiState) {
    private val mutableState = MutableStateFlow(initialState)
    val state: AlchemistUiState
        get() = mutableState.value
    val stateFlow: StateFlow<AlchemistUiState> = mutableState.asStateFlow()

    /**
     * Replace the current state.
     */
    fun set(newState: AlchemistUiState) {
        mutableState.value = newState
    }

    /**
     * Mutate the current state atomically.
     */
    fun update(transform: (AlchemistUiState) -> AlchemistUiState) {
        mutableState.update(transform)
    }
}

/**
 * Bundles state storage and user callbacks for platform entrypoints.
 */
data class ComposeUiController(val store: ComposeUiStateStore, val callbacks: AlchemistUiCallbacks)

/**
 * Demo controller used by JS/WASM entrypoints and as a fallback shell.
 */
fun demoController(): ComposeUiController {
    val store = ComposeUiStateStore(sampleUiState())
    val callbacks =
        object : AlchemistUiCallbacks {
            override suspend fun onPlay() {
                store.update {
                    it.copy(
                        controls = it.controls.copy(status = SimulationStatus.RUNNING),
                    )
                }
            }

            override suspend fun onPause() {
                store.update {
                    it.copy(
                        controls = it.controls.copy(status = SimulationStatus.PAUSED),
                    )
                }
            }

            override suspend fun onStep() {
                store.update {
                    val nextStep = it.controls.step + 1
                    it.withStepProgress(nextStep)
                }
            }

            override suspend fun onToTimeInputChanged(value: String) {
                store.update {
                    it.copy(controls = it.controls.copy(toTimeInput = value))
                }
            }

            override suspend fun onToTimeSubmit() {
                store.update { state ->
                    val target = state.controls.toTimeInput.toDoubleOrNull()
                        ?: return@update state.withDialog("Invalid time", "Insert a valid numeric time.")
                    val currentTime = state.controls.timeLabel.toDoubleOrNull() ?: 0.0
                    val wasRunning = state.controls.status == SimulationStatus.RUNNING
                    if (target < currentTime) {
                        return@update state.withDialog(
                            title = "Invalid time",
                            message = "Target time $target cannot be lower than current time $currentTime.",
                        )
                    }
                    val targetStep = ceil(target * DEMO_TIME_SCALE).toLong()
                    state.withStepProgress(
                        targetStep,
                        timeLabel = target.formatFixed(DISPLAYED_TIME_DECIMALS),
                        status = if (wasRunning) SimulationStatus.RUNNING else SimulationStatus.PAUSED,
                    )
                }
            }

            override suspend fun onToStepInputChanged(value: String) {
                store.update {
                    it.copy(controls = it.controls.copy(toStepInput = value))
                }
            }

            override suspend fun onToStepSubmit() {
                store.update { state ->
                    val target = state.controls.toStepInput.toLongOrNull()
                        ?: return@update state.withDialog("Invalid step", "Insert a valid integer step.")
                    val wasRunning = state.controls.status == SimulationStatus.RUNNING
                    if (target < state.controls.step) {
                        return@update state.withDialog(
                            title = "Invalid step",
                            message = "Target step $target cannot be lower than current step ${state.controls.step}.",
                        )
                    }
                    state.withStepProgress(
                        target,
                        status = if (wasRunning) SimulationStatus.RUNNING else SimulationStatus.PAUSED,
                    )
                }
            }

            override suspend fun onFpsInputChanged(value: String) {
                store.update {
                    it.copy(controls = it.controls.copy(fpsInput = value))
                }
            }

            override suspend fun onFpsSubmit() {
                store.update { state ->
                    val target = state.controls.fpsInput.toIntOrNull()
                        ?: return@update state.withDialog("Invalid FPS", "Insert a valid integer FPS value.")
                    state.copy(controls = state.controls.withUiFps(target))
                }
            }

            override suspend fun onEventRateChanged(value: Float) {
                store.update { state ->
                    state.copy(controls = state.controls.updateEventThrottling(value.roundToInt()))
                }
            }

            override suspend fun onNodeSelected(nodeId: Int) {
                store.update {
                    it.withSelection(listOf(nodeId))
                }
            }

            override suspend fun onNodesSelected(nodeIds: List<Int>) {
                store.update {
                    it.withSelection(nodeIds)
                }
            }

            override suspend fun onNodesMoved(nodePositions: List<NodePositionUpdate>) {
                store.update { currentState ->
                    currentState
                        .copy(scene = currentState.scene.withMovedNodes(nodePositions))
                        .withSelection(currentState.selectedNodeIds)
                }
            }

            override suspend fun onInspectorDismiss() {
                store.update {
                    it.withSelection(emptyList())
                }
            }

            override suspend fun onToggleLinks() {
                store.update {
                    it.copy(
                        scene = it.scene.copy(showLinks = !it.scene.showLinks),
                    )
                }
            }

            override suspend fun onDialogDismiss() {
                store.update {
                    it.copy(controls = it.controls.copy(dialog = null))
                }
            }
        }
    return ComposeUiController(store, callbacks)
}

private fun sampleUiState(): AlchemistUiState {
    val nodes = persistentListOf(
        ViewportNode(
            id = 1,
            coordinates = persistentListOf(-3.5, 1.7),
            accent = 0.15f,
            metadata = persistentListOf(
                Labeled("Neighbors", "4"),
                Labeled("Reactions", "3"),
                Labeled("Properties", "2"),
            ),
            concentrations = persistentListOf(
                Labeled("signal", "0.91"),
                Labeled("gradient", "0.42"),
            ),
        ),
        ViewportNode(
            id = 2,
            coordinates = persistentListOf(-1.2, 0.3),
            accent = 0.33f,
            metadata = persistentListOf(
                Labeled("Neighbors", "5"),
                Labeled("Reactions", "2"),
                Labeled("Properties", "1"),
            ),
            concentrations = persistentListOf(
                Labeled("source", "true"),
                Labeled("gradient", "0.68"),
            ),
        ),
        ViewportNode(
            id = 3,
            coordinates = persistentListOf(0.8, 2.2),
            accent = 0.55f,
            metadata = persistentListOf(
                Labeled("Neighbors", "3"),
                Labeled("Reactions", "4"),
                Labeled("Properties", "2"),
            ),
            concentrations = persistentListOf(
                Labeled("signal", "0.77"),
                Labeled("temperature", "296 K"),
            ),
        ),
        ViewportNode(
            id = 4,
            coordinates = persistentListOf(2.1, -0.8),
            accent = 0.74f,
            metadata = persistentListOf(
                Labeled("Neighbors", "6"),
                Labeled("Reactions", "2"),
                Labeled("Properties", "3"),
            ),
            concentrations = persistentListOf(
                Labeled("gradient", "0.18"),
                Labeled("payload", "ready"),
            ),
        ),
        ViewportNode(
            id = 5,
            coordinates = persistentListOf(3.9, 1.4),
            accent = 0.92f,
            metadata = persistentListOf(
                Labeled("Neighbors", "2"),
                Labeled("Reactions", "1"),
                Labeled("Properties", "1"),
            ),
            concentrations = persistentListOf(
                Labeled("goal", "true"),
                Labeled("signal", "0.12"),
            ),
        ),
    )
    return AlchemistUiState(
        scene = ViewportScene(
            nodes = nodes,
            edges = persistentListOf(
                ViewportEdge(1, 2),
                ViewportEdge(2, 3),
                ViewportEdge(3, 4),
                ViewportEdge(4, 5),
                ViewportEdge(1, 3),
            ),
            dimensions = 2,
            summary = persistentListOf(
                Labeled("Nodes", nodes.size),
                Labeled("Dimensions", "2D"),
            ),
            message = "Attach a simulation monitor to replace this demo scenario.",
        ),
        controls = SimulationControlsState(
            status = SimulationStatus.PAUSED,
            timeLabel = formatDemoTime(42),
            step = 42,
            progress = SimulationProgress(
                fraction = 0.42f,
                label = "Scenario exploration",
            ),
        ),
    )
}

internal fun AlchemistUiState.withDialog(title: String, message: String): AlchemistUiState = copy(
    controls = controls.copy(
        dialog = ControlDialogState(title, message),
    ),
)

internal fun AlchemistUiState.withStepProgress(
    step: Long,
    timeLabel: String = formatDemoTime(step),
    status: SimulationStatus = SimulationStatus.PAUSED,
): AlchemistUiState = copy(
    controls = controls.copy(
        status = status,
        step = step,
        timeLabel = timeLabel,
        progress = SimulationProgress(
            fraction = (step % 100).toFloat() / 100f,
            label = "Scenario exploration",
        ),
        dialog = null,
    ),
)

internal fun SimulationControlsState.withUiFps(target: Int): SimulationControlsState {
    val coerced = target.coerceIn(MIN_UI_FPS, maxUiFps)
    return copy(
        fpsInput = coerced.toString(),
        uiFps = coerced,
        dialog = null,
    )
}

// internal fun SimulationControlsState.withEventRateSliderValue(target: Int): SimulationControlsState = copy(
//    eventRateSliderValue = target.coerceIn(MIN_SIMULATION_EVENTS_PER_SECOND, simulationEventThrottling.value),
//    dialog = null,
// )

internal fun AlchemistUiState.withSelection(nodeIds: List<Int>): AlchemistUiState {
    val selectedIds = scene.sanitizeSelection(nodeIds)
    return copy(
        selectedNodeIds = selectedIds,
        inspector = scene.toInspectorState(selectedIds),
    )
}

internal fun ViewportScene.sanitizeSelection(nodeIds: List<Int>): ImmutableList<Int> {
    val availableNodeIds = nodes.mapTo(linkedSetOf()) { it.id }
    return nodeIds.distinct().filter(availableNodeIds::contains).toImmutableList()
}

internal fun ViewportScene.withMovedNodes(nodePositions: List<NodePositionUpdate>): ViewportScene {
    if (nodePositions.isEmpty()) {
        return this
    }
    val coordinatesByNodeId = nodePositions.associate { it.nodeId to it.coordinates }
    val updatedNodes = nodes.map { node ->
        coordinatesByNodeId[node.id]?.let(node::withCoordinates) ?: node
    }.toImmutableList()
    return copy(
        nodes = updatedNodes,
        worldBounds = updatedNodes.toWorldBounds(),
    )
}

internal fun ViewportScene.translateSelectedNodes(
    nodeIds: Collection<Int>,
    deltaX: Double,
    deltaY: Double,
): ViewportScene {
    if (nodeIds.isEmpty() || (deltaX == 0.0 && deltaY == 0.0)) {
        return this
    }
    val selectedIds = nodeIds.toHashSet()
    val updatedNodes = nodes.map { node ->
        if (node.id in selectedIds) {
            node.translate(deltaX, deltaY)
        } else {
            node
        }
    }.toImmutableList()
    return copy(
        nodes = updatedNodes,
        worldBounds = updatedNodes.toWorldBounds(),
    )
}

internal fun ViewportScene.toInspectorState(selectedNodeIds: List<Int>): InspectorState? {
    if (selectedNodeIds.isEmpty()) {
        return null
    }
    val selectedNodesById = nodes.associateBy { it.id }
    val selectedNodes = selectedNodeIds.mapNotNull(selectedNodesById::get)
    return when (selectedNodes.size) {
        0 -> null
        1 -> selectedNodes.single().toInspectorState()
        else -> selectedNodes.toGroupInspectorState()
    }
}

internal fun ViewportNode.toInspectorState(): SingleNode = SingleNode(
    nodeId = id,
    subtitle = "Live node snapshot",
    position = coordinates.take(2).mapIndexed { index, coordinate ->
        Labeled(if (index == 0) "X" else "Y", coordinate)
    }.toImmutableList(),
    concentrations = concentrations,
    metadata = metadata,
)

internal fun ViewportNode.translate(deltaX: Double, deltaY: Double): ViewportNode = copy(
    coordinates = coordinates.mapIndexed { index, coordinate ->
        when (index) {
            0 -> coordinate + deltaX
            1 -> coordinate + deltaY
            else -> coordinate
        }
    }.toImmutableList(),
)

internal fun ViewportNode.withCoordinates(newCoordinates: List<Double>): ViewportNode =
    copy(coordinates = newCoordinates.toImmutableList())

internal fun ViewportNode.toPositionUpdate(): NodePositionUpdate = NodePositionUpdate(id, coordinates)

internal fun List<ViewportNode>.toGroupInspectorState(): NodeGroup {
    val xs = map { it.coordinates[0] }
    val ys = map { it.coordinates[1] }
    val moleculeNames = flatMap { node -> node.concentrations.map(Labeled<Any?>::label) }.distinct().sorted()
    val concentrations = moleculeNames.map { molecule ->
        val values = map { node -> node.concentrations.firstOrNull { it.label == molecule }?.value }
        val sharedValue = values.firstOrNull()?.takeIf { firstValue ->
            values.all { it == firstValue }
        }
        Labeled(molecule, sharedValue ?: MIXED_CONCENTRATION_PLACEHOLDER)
    }.toImmutableList()
    return NodeGroup(
        nodeIds = map(ViewportNode::id).toImmutableList(),
        position = persistentListOf(
            Labeled("Min X", xs.min()),
            Labeled("Max X", xs.max()),
            Labeled("Min Y", ys.min()),
            Labeled("Max Y", ys.max()),
        ),
        concentrations = concentrations,
    )
}

internal fun Double.formatFixed(decimals: Int): String {
    val safeDecimals = decimals.coerceAtLeast(0)
    val factor = (1..safeDecimals).fold(1.0) { acc, _ -> acc * 10.0 }
    val rounded = kotlin.math.round(this * factor) / factor
    val raw = rounded.toString()
    return when {
        safeDecimals == 0 -> raw.substringBefore('.')
        '.' !in raw -> raw + "." + "0".repeat(safeDecimals)
        else -> {
            val fractional = raw.substringAfter('.')
            raw + "0".repeat((safeDecimals - fractional.length).coerceAtLeast(0))
        }
    }
}

private fun formatDemoTime(step: Long): String = (step / 10.0).formatFixed(2)

private const val DEMO_TIME_SCALE = 10.0
internal const val MIXED_CONCENTRATION_PLACEHOLDER = "Mixed"
