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
data class InfoField(val label: String, val value: String)

/**
 * A node projected in the central viewport.
 */
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
 * State of the central scene area.
 */
data class ViewportScene(
    val nodes: List<ViewportNode> = emptyList(),
    val dimensions: Int = 2,
    val backdrop: ViewportBackdrop = ViewportBackdrop.SPACE,
    val summary: List<InfoField> = emptyList(),
    val message: String = "Waiting for simulation data",
)

/**
 * State for the bottom control dock progress section.
 */
data class SimulationProgress(val fraction: Float? = null, val label: String = "Progress unavailable") {
    init {
        require(fraction == null || fraction in 0f..1f) {
            "Progress fraction must be null or within [0, 1]."
        }
    }
}

/**
 * State for transport controls and simulator metrics.
 */
data class SimulationControlsState(
    val status: SimulationStatus = SimulationStatus.INIT,
    val timeLabel: String = "0",
    val step: Long = 0L,
    val progress: SimulationProgress = SimulationProgress(),
) {
    val canPlay: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED
    val canPause: Boolean = status == SimulationStatus.RUNNING
    val canStep: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED
    val statusLabel: String = status.name.lowercase().replaceFirstChar(Char::uppercaseChar)
}

/**
 * State for the node inspector panel.
 */
data class NodeInspectorState(
    val nodeId: Int,
    val title: String = "Node $nodeId",
    val subtitle: String,
    val position: List<InfoField>,
    val concentrations: List<InfoField>,
    val metadata: List<InfoField>,
)

/**
 * Top-level state consumed by the Compose UI shell.
 */
data class AlchemistUiState(
    val scene: ViewportScene = ViewportScene(),
    val controls: SimulationControlsState = SimulationControlsState(),
    val selectedNodeId: Int? = null,
    val inspector: NodeInspectorState? = null,
)

/**
 * Interaction contract expected by the common Compose UI.
 */
interface AlchemistUiCallbacks {
    suspend fun onPlay()

    suspend fun onPause()

    suspend fun onStep()

    suspend fun onNodeSelected(nodeId: Int)

    suspend fun onInspectorDismiss()
}

/**
 * Shared no-op callback implementation.
 */
object NoOpUiCallbacks : AlchemistUiCallbacks {
    override suspend fun onPlay() = Unit

    override suspend fun onPause() = Unit

    override suspend fun onStep() = Unit

    override suspend fun onNodeSelected(nodeId: Int) = Unit

    override suspend fun onInspectorDismiss() = Unit
}
