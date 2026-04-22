@file:Suppress("UndocumentedPublicFunction", "UndocumentedPublicProperty")

/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.runtime.Immutable

/**
 * High-level simulation status mirrored in the Compose UI.
 */
enum class SimulationStatus {
    INIT,
    READY,
    PAUSED,
    RUNNING,
    TERMINATED,
}

/**
 * Background treatment of the central viewport.
 */
enum class ViewportBackdrop {
    SPACE,
    MAP,
}

/**
 * A simple label/value pair for inspector and summary sections.
 */
@Immutable
data class InfoField(val label: String, val value: String)

/**
 * A node projected in the central viewport.
 */
@Immutable
data class ViewportNode(
    val id: Int,
    val coordinates: List<Double>,
    val accent: Float = 0.5f,
    val metadata: List<InfoField> = emptyList(),
    val concentrations: List<InfoField> = emptyList(),
) {
    init {
        require(coordinates.size >= 2) {
            "Viewport nodes require at least two coordinates."
        }
    }
}

/**
 * An undirected edge projected in the central viewport.
 */
@Immutable
data class ViewportEdge(val fromNodeId: Int, val toNodeId: Int) {
    init {
        require(fromNodeId != toNodeId) {
            "Viewport edges require two distinct endpoints."
        }
    }
}

/**
 * State of the central scene area.
 */
@Immutable
data class ViewportScene(
    val nodes: List<ViewportNode> = emptyList(),
    val edges: List<ViewportEdge> = emptyList(),
    val showLinks: Boolean = false,
    val dimensions: Int = 2,
    val backdrop: ViewportBackdrop = ViewportBackdrop.SPACE,
    val summary: List<InfoField> = emptyList(),
    val message: String = "Waiting for simulation data",
)

/**
 * State for the bottom control dock progress section.
 */
@Immutable
data class SimulationProgress(val fraction: Float? = null, val label: String = "Progress unavailable") {
    init {
        require(fraction == null || fraction in 0f..1f) {
            "Progress fraction must be null or within [0, 1]."
        }
    }
}

/**
 * Modal error state shown by the shared UI.
 */
@Immutable
data class ControlDialogState(val title: String, val message: String)

/**
 * State for transport controls and simulator metrics.
 */
@Immutable
data class SimulationControlsState(
    val status: SimulationStatus = SimulationStatus.INIT,
    val timeLabel: String = "0",
    val step: Long = 0L,
    val progress: SimulationProgress = SimulationProgress(),
    val toTimeInput: String = "",
    val toStepInput: String = "",
    val fpsInput: String = DEFAULT_UI_FPS.toString(),
    val uiFps: Int = DEFAULT_UI_FPS,
    val maxUiFps: Int = DEFAULT_MAX_UI_FPS,
    val eventRateSliderValue: Int = MIN_SIMULATION_EVENTS_PER_SECOND,
    val maxEventRateSliderValue: Int = DEFAULT_MAX_SIMULATION_EVENTS_PER_SECOND,
    val dialog: ControlDialogState? = null,
) {
    init {
        require(maxUiFps >= MIN_UI_FPS) {
            "Maximum UI fps must be at least $MIN_UI_FPS."
        }
        require(uiFps in MIN_UI_FPS..maxUiFps) {
            "UI fps must remain within [$MIN_UI_FPS, $maxUiFps]."
        }
        require(eventRateSliderValue in MIN_SIMULATION_EVENTS_PER_SECOND..maxEventRateSliderValue) {
            "Slider value must remain within [$MIN_SIMULATION_EVENTS_PER_SECOND, $maxEventRateSliderValue]."
        }
        require(maxEventRateSliderValue > MIN_SIMULATION_EVENTS_PER_SECOND) {
            "Maximum slider value must exceed the minimum event rate."
        }
    }

    val canPlay: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED
    val canPause: Boolean = status == SimulationStatus.RUNNING
    val canStep: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED
    val statusLabel: String = status.name.lowercase().replaceFirstChar(Char::uppercaseChar)
    val isFullThrottle: Boolean = eventRateSliderValue == maxEventRateSliderValue
    val effectiveEventsPerSecond: Int? = eventRateSliderValue.takeUnless { isFullThrottle }
    val eventRateLabel: String =
        effectiveEventsPerSecond?.let { "$it evt/s" } ?: FULL_THROTTLE_LABEL
    val fpsRangeLabel: String = "$MIN_UI_FPS-$maxUiFps FPS"
}

/**
 * State for the right-side inspector panel.
 */
@Immutable
sealed interface InspectorState

/**
 * Inspector state for a single node.
 */
@Immutable
data class NodeInspectorState(
    val nodeId: Int,
    val title: String = "Node $nodeId",
    val subtitle: String,
    val position: List<InfoField>,
    val concentrations: List<InfoField>,
    val metadata: List<InfoField>,
) : InspectorState

/**
 * Inspector state for a group of selected nodes.
 */
@Immutable
data class GroupInspectorState(
    val nodeIds: List<Int>,
    val title: String = "Selected Nodes",
    val subtitle: String = "${nodeIds.size} nodes selected",
    val position: List<InfoField>,
    val concentrations: List<InfoField>,
) : InspectorState

/**
 * Top-level state consumed by the Compose UI shell.
 */
@Immutable
data class AlchemistUiState(
    val scene: ViewportScene = ViewportScene(),
    val controls: SimulationControlsState = SimulationControlsState(),
    val selectedNodeIds: List<Int> = emptyList(),
    val inspector: InspectorState? = null,
)

/**
 * Interaction contract expected by the common Compose UI.
 */
interface AlchemistUiCallbacks {
    suspend fun onPlay()

    suspend fun onPause()

    suspend fun onStep()

    suspend fun onToTimeInputChanged(value: String)

    suspend fun onToTimeSubmit()

    suspend fun onToStepInputChanged(value: String)

    suspend fun onToStepSubmit()

    suspend fun onFpsInputChanged(value: String)

    suspend fun onFpsSubmit()

    suspend fun onEventRateChanged(value: Float)

    suspend fun onNodeSelected(nodeId: Int)

    suspend fun onNodesSelected(nodeIds: List<Int>)

    suspend fun onInspectorDismiss()

    suspend fun onToggleLinks()

    suspend fun onDialogDismiss()
}

/**
 * Shared no-op callback implementation.
 */
object NoOpUiCallbacks : AlchemistUiCallbacks {
    override suspend fun onPlay() = Unit

    override suspend fun onPause() = Unit

    override suspend fun onStep() = Unit

    override suspend fun onToTimeInputChanged(value: String) = Unit

    override suspend fun onToTimeSubmit() = Unit

    override suspend fun onToStepInputChanged(value: String) = Unit

    override suspend fun onToStepSubmit() = Unit

    override suspend fun onFpsInputChanged(value: String) = Unit

    override suspend fun onFpsSubmit() = Unit

    override suspend fun onEventRateChanged(value: Float) = Unit

    override suspend fun onNodeSelected(nodeId: Int) = Unit

    override suspend fun onNodesSelected(nodeIds: List<Int>) = Unit

    override suspend fun onInspectorDismiss() = Unit

    override suspend fun onToggleLinks() = Unit

    override suspend fun onDialogDismiss() = Unit
}
