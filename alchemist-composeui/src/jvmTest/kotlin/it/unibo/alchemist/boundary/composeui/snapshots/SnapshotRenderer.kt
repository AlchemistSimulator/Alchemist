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
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
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
import kotlin.math.abs
import kotlin.math.sign
import org.jetbrains.skia.EncodedImageFormat
import org.yaml.snakeyaml.Yaml

/** Steps each scenario runs before its snapshots. */
private const val STEPS = 2_000L

/** Frames rendered before and after the scripted input, so that layout and animations settle. */
private const val SETTLE_FRAMES = 60
private const val FRAME_NANOS = 16_666_667L

/** The key of a scenario file that lists its snapshots; the loader ignores keys starting with `_`. */
private const val SNAPSHOTS_KEY = "_snapshots"

/**
 * Renders the whole UI offscreen for every `.yml` scenario in the folder `args[0]`, writing
 * `<scenario>-<snapshot>.png` into the folder `args[1]`: review images for people and agents, not assertions.
 * A scenario lists its snapshots under `_snapshots` (see [SnapshotSpec.of]); otherwise it gets [SnapshotSpec.DEFAULTS].
 */
fun main(args: Array<String>) {
    val (scenarios, output) = args.map(::File)
    output.mkdirs()
    scenarios.listFiles { file -> file.extension == "yml" }.orEmpty().sorted().forEach { scenario ->
        val state = scenario.runToSnapshot()
        scenario.snapshots().forEach { spec ->
            val target = output.resolve("${scenario.nameWithoutExtension}-${spec.name}.png")
            target.writeBytes(state.render(spec))
            println("Rendered $target")
        }
    }
}

private fun File.snapshots(): List<SnapshotSpec> =
    (Yaml().load<Map<String, Any?>>(readText())[SNAPSHOTS_KEY] as? List<*>)
        ?.map { entry -> SnapshotSpec.of(entry as? Map<*, *> ?: error("Not a snapshot: $entry")) }
        ?: SnapshotSpec.DEFAULTS

/**
 * Loads this scenario, runs it for [STEPS] steps, and returns the UI state of the result,
 * links shown and every node inspected, as snapshots may select any of them.
 */
private fun File.runToSnapshot(): AlchemistUiState {
    val simulation: Simulation<Any, Euclidean2DPosition> = LoadAlchemist.from(this).getDefault()
    simulation.environment.addTerminator { simulation.step >= STEPS }
    simulation.play()
    simulation.run()
    return AlchemistUiState(
        scene = simulation.environment
            .toViewport(renderLinks = true, inspected = simulation.environment.nodes.current.map { it.id })
            .copy(showLinks = true),
        controls = SimulationControlsState(
            status = SimulationStatus.PAUSED,
            time = simulation.currentTime.toDouble(),
            step = simulation.step,
        ),
    )
}

/**
 * Renders this state as [spec] asks: input first, on the bare UI, so that panels opened later do not take it;
 * then the selection and the value editor.
 */
private fun AlchemistUiState.render(spec: SnapshotSpec): ByteArray {
    val controller = ComposeUiController(this) { NoOpUiCallbacks }
    val scene = ImageComposeScene(width = spec.width, height = spec.height, density = Density(1f)) { app(controller) }
    return try {
        repeat(SETTLE_FRAMES) { frame -> scene.render(frame * FRAME_NANOS).close() }
        scene.play(spec)
        controller.store.update(spec::applyTo)
        repeat(SETTLE_FRAMES) { frame -> scene.render((SETTLE_FRAMES + frame) * FRAME_NANOS).close() }
        val image = scene.render(2L * SETTLE_FRAMES * FRAME_NANOS)
        try {
            checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) { "Cannot encode the snapshot as PNG" }.bytes
        } finally {
            image.close()
        }
    } finally {
        scene.close()
    }
}

/** Sends the input of [spec]: wheel notches and a right-button drag at the centre, then the keys. */
@OptIn(InternalComposeUiApi::class)
private fun ImageComposeScene.play(spec: SnapshotSpec) {
    val center = Offset(spec.width / 2f, spec.height / 2f)
    repeat(abs(spec.zoom)) {
        sendPointerEvent(PointerEventType.Scroll, center, scrollDelta = Offset(0f, -sign(spec.zoom.toFloat())))
    }
    if (spec.pan != Offset.Zero) {
        val secondary = PointerButtons(isSecondaryPressed = true)
        sendPointerEvent(PointerEventType.Press, center, buttons = secondary, button = PointerButton.Secondary)
        sendPointerEvent(PointerEventType.Move, center + spec.pan, buttons = secondary)
        sendPointerEvent(PointerEventType.Release, center + spec.pan, button = PointerButton.Secondary)
    }
    spec.keys.forEach { key ->
        // Compose builds key events only through an internal factory, opted in above: the alternative is AWT events.
        sendKeyEvent(KeyEvent(key, KeyEventType.KeyDown))
        sendKeyEvent(KeyEvent(key, KeyEventType.KeyUp))
    }
}
