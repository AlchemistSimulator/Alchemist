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
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.ControlDialogState
import it.unibo.alchemist.boundary.composeui.model.GroupInspectorState
import it.unibo.alchemist.boundary.composeui.model.InfoField
import it.unibo.alchemist.boundary.composeui.model.InspectorState
import it.unibo.alchemist.boundary.composeui.model.NodeInspectorState
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationProgress
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.ViewportBackdrop
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thread-safe holder for the UI state observed by Compose.
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
    val nodes = listOf(
        ViewportNode(
            id = 1,
            coordinates = listOf(-3.5, 1.7),
            accent = 0.15f,
            metadata = listOf(
                InfoField("Neighbors", "4"),
                InfoField("Reactions", "3"),
                InfoField("Properties", "2"),
            ),
            concentrations = listOf(
                InfoField("signal", "0.91"),
                InfoField("gradient", "0.42"),
            ),
        ),
        ViewportNode(
            id = 2,
            coordinates = listOf(-1.2, 0.3),
            accent = 0.33f,
            metadata = listOf(
                InfoField("Neighbors", "5"),
                InfoField("Reactions", "2"),
                InfoField("Properties", "1"),
            ),
            concentrations = listOf(
                InfoField("source", "true"),
                InfoField("gradient", "0.68"),
            ),
        ),
        ViewportNode(
            id = 3,
            coordinates = listOf(0.8, 2.2),
            accent = 0.55f,
            metadata = listOf(
                InfoField("Neighbors", "3"),
                InfoField("Reactions", "4"),
                InfoField("Properties", "2"),
            ),
            concentrations = listOf(
                InfoField("signal", "0.77"),
                InfoField("temperature", "296 K"),
            ),
        ),
        ViewportNode(
            id = 4,
            coordinates = listOf(2.1, -0.8),
            accent = 0.74f,
            metadata = listOf(
                InfoField("Neighbors", "6"),
                InfoField("Reactions", "2"),
                InfoField("Properties", "3"),
            ),
            concentrations = listOf(
                InfoField("gradient", "0.18"),
                InfoField("payload", "ready"),
            ),
        ),
        ViewportNode(
            id = 5,
            coordinates = listOf(3.9, 1.4),
            accent = 0.92f,
            metadata = listOf(
                InfoField("Neighbors", "2"),
                InfoField("Reactions", "1"),
                InfoField("Properties", "1"),
            ),
            concentrations = listOf(
                InfoField("goal", "true"),
                InfoField("signal", "0.12"),
            ),
        ),
    )
    return AlchemistUiState(
        scene = ViewportScene(
            nodes = nodes,
            edges = listOf(
                ViewportEdge(1, 2),
                ViewportEdge(2, 3),
                ViewportEdge(3, 4),
                ViewportEdge(4, 5),
                ViewportEdge(1, 3),
            ),
            dimensions = 2,
            backdrop = ViewportBackdrop.SPACE,
            summary = listOf(
                InfoField("Nodes", nodes.size.toString()),
                InfoField("Dimensions", "2D"),
                InfoField("Backdrop", "Procedural field"),
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

//internal fun SimulationControlsState.withEventRateSliderValue(target: Int): SimulationControlsState = copy(
//    eventRateSliderValue = target.coerceIn(MIN_SIMULATION_EVENTS_PER_SECOND, simulationEventThrottling.value),
//    dialog = null,
//)

internal fun AlchemistUiState.withSelection(nodeIds: List<Int>): AlchemistUiState {
    val selectedIds = scene.sanitizeSelection(nodeIds)
    return copy(
        selectedNodeIds = selectedIds,
        inspector = scene.toInspectorState(selectedIds),
    )
}

internal fun ViewportScene.sanitizeSelection(nodeIds: List<Int>): List<Int> {
    val availableNodeIds = nodes.mapTo(linkedSetOf()) { it.id }
    return nodeIds.distinct().filter(availableNodeIds::contains)
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

internal fun ViewportNode.toInspectorState(): NodeInspectorState = NodeInspectorState(
    nodeId = id,
    subtitle = "Live node snapshot",
    position = coordinates.take(2).mapIndexed { index, coordinate ->
        InfoField(if (index == 0) "X" else "Y", coordinate.formatFixed(3))
    },
    concentrations = concentrations,
    metadata = metadata,
)

internal fun List<ViewportNode>.toGroupInspectorState(): GroupInspectorState {
    val xs = map { it.coordinates[0] }
    val ys = map { it.coordinates[1] }
    val moleculeNames = flatMap { node -> node.concentrations.map(InfoField::label) }.distinct().sorted()
    val concentrations = moleculeNames.map { molecule ->
        val values = map { node -> node.concentrations.firstOrNull { it.label == molecule }?.value }
        val sharedValue = values.firstOrNull()?.takeIf { firstValue ->
            values.all { it == firstValue }
        }
        InfoField(molecule, sharedValue ?: MIXED_CONCENTRATION_PLACEHOLDER)
    }
    return GroupInspectorState(
        nodeIds = map(ViewportNode::id),
        position = listOf(
            InfoField("Min X", xs.minOrNull()?.formatFixed(3).orEmpty()),
            InfoField("Max X", xs.maxOrNull()?.formatFixed(3).orEmpty()),
            InfoField("Min Y", ys.minOrNull()?.formatFixed(3).orEmpty()),
            InfoField("Max Y", ys.maxOrNull()?.formatFixed(3).orEmpty()),
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
