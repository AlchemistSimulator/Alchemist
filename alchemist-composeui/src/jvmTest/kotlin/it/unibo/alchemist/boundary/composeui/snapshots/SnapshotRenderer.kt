/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.snapshots

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.toViewport
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.callbacks.NoOpUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.app
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.io.File
import org.jetbrains.skia.EncodedImageFormat

/** Steps each scenario runs before its snapshot. */
private const val STEPS = 2_000L

/** Nodes selected for the snapshot with the inspector open. */
private const val SELECTED_NODES = 4

/** Frames rendered before the snapshot, so that layout settles and the inspector finishes sliding in. */
private const val FRAMES = 60
private const val FRAME_NANOS = 16_666_667L

/**
 * Renders the whole UI offscreen for every `.yml` scenario in the folder `args[0]`, writing
 * `<scenario>-<size>.png` into the folder `args[1]`: review images for people and agents, not assertions.
 */
fun main(args: Array<String>) {
    val (scenarios, output) = args.map(::File)
    output.mkdirs()
    scenarios.listFiles { file -> file.extension == "yml" }.orEmpty().sorted().forEach { scenario ->
        val state = scenario.runToSnapshot()
        Size.entries.forEach { size ->
            val target = output.resolve("${scenario.nameWithoutExtension}-${size.label}.png")
            target.writeBytes(state.withSelectionFor(size).render(size))
            println("Rendered $target")
        }
    }
}

/** The window sizes of the snapshots. */
private enum class Size(val label: String, val width: Int, val height: Int, val inspector: Boolean) {
    WIDE("wide", 1920, 1080, inspector = false),
    COMPACT("compact", 1024, 600, inspector = false),
    INSPECTOR("inspector", 1920, 1080, inspector = true),
}

/** Loads this scenario, runs it for [STEPS] steps, and returns the UI state of the result, links shown. */
private fun File.runToSnapshot(): AlchemistUiState {
    val simulation: Simulation<Any, Euclidean2DPosition> = LoadAlchemist.from(this).getDefault()
    simulation.environment.addTerminator { simulation.step >= STEPS }
    simulation.play()
    simulation.run()
    return AlchemistUiState(
        scene = simulation.environment.toViewport(renderLinks = true).copy(showLinks = true),
        controls = SimulationControlsState(
            status = SimulationStatus.PAUSED,
            time = simulation.currentTime.toDouble(),
            step = simulation.step,
        ),
    )
}

private fun AlchemistUiState.withSelectionFor(size: Size): AlchemistUiState =
    if (size.inspector) withSelection(scene.nodes.map { it.id }.sorted().take(SELECTED_NODES)) else this

private fun AlchemistUiState.render(size: Size): ByteArray {
    val controller = ComposeUiController(this) { NoOpUiCallbacks }
    val scene = ImageComposeScene(width = size.width, height = size.height, density = Density(1f)) { app(controller) }
    return try {
        repeat(FRAMES) { frame -> scene.render(frame * FRAME_NANOS).close() }
        val image = scene.render(FRAMES * FRAME_NANOS)
        try {
            checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) { "Cannot encode the snapshot as PNG" }.bytes
        } finally {
            image.close()
        }
    } finally {
        scene.close()
    }
}
