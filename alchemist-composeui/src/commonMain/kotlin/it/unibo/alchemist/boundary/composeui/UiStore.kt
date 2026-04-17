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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotApplyConflictException

/**
 * Thread-safe holder for the UI state observed by Compose.
 */
class ComposeUiStateStore(initialState: AlchemistUiState) {
    var state: AlchemistUiState by mutableStateOf(initialState)
        private set

    /**
     * Replace the current state.
     */
    fun set(newState: AlchemistUiState) {
        mutateState {
            state = newState
        }
    }

    /**
     * Mutate the current state atomically.
     */
    fun update(transform: (AlchemistUiState) -> AlchemistUiState) {
        mutateState {
            state = transform(state)
        }
    }

    /**
     * Compose snapshots are optimistic: concurrent writers may race, and the loser must retry.
     */
    private fun mutateState(mutation: () -> Unit) {
        runCatching {
            Snapshot.withMutableSnapshot {
                mutation()
            }
        }.getOrElse { error ->
            when (error) {
                is SnapshotApplyConflictException -> mutateState(mutation)
                else -> throw error
            }
        }
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
                    it.copy(
                        controls = it.controls.copy(
                            status = SimulationStatus.PAUSED,
                            step = nextStep,
                            timeLabel = formatDemoTime(nextStep),
                            progress = SimulationProgress(
                                fraction = (nextStep % 100).toFloat() / 100f,
                                label = "Scenario exploration",
                            ),
                        ),
                    )
                }
            }

            override suspend fun onNodeSelected(nodeId: Int) {
                val node = store.state.scene.nodes.firstOrNull { it.id == nodeId } ?: return
                store.update {
                    it.copy(
                        selectedNodeId = nodeId,
                        inspector = node.toInspectorState(),
                    )
                }
            }

            override suspend fun onInspectorDismiss() {
                store.update {
                    it.copy(selectedNodeId = null, inspector = null)
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

internal fun ViewportNode.toInspectorState(): NodeInspectorState = NodeInspectorState(
    nodeId = id,
    subtitle = "Live node snapshot",
    position = coordinates.take(2).mapIndexed { index, coordinate ->
        InfoField(if (index == 0) "X" else "Y", coordinate.formatFixed(3))
    },
    concentrations = concentrations,
    metadata = metadata,
)

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
