/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MAX_UI_FPS
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.toViewport
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistSimulations.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.QuickJumps
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.resources.Res
import it.unibo.alchemist.boundary.composeui.resources.alchemist_logo
import it.unibo.alchemist.boundary.composeui.view.app
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.util.concurrent.atomic.AtomicBoolean
import org.jetbrains.compose.resources.painterResource
import org.slf4j.LoggerFactory

/**
 * Shows the simulation in a desktop window.
 * Closing the window, or pressing Ctrl+Q, terminates the simulation, so exporters can finish and Alchemist can exit.
 * On a JVM without a display, it logs a warning and does nothing, unless [failOnHeadless] is true.
 *
 * @param targetFps how many times per second the UI redraws at startup.
 * @param failOnHeadless whether to fail, instead of warning, when there is no display.
 * @param timeJumps the time quick jumps, by default 10, 100 and 1000 time units.
 * @param stepJumps the step quick jumps, by default one, two and ten steps per node.
 */
class ComposeMonitor<T, P : Position<P>> @JvmOverloads constructor(
    targetFps: Int = 30,
    failOnHeadless: Boolean = false,
    timeJumps: List<Number> = QuickJumps().time,
    stepJumps: List<Number>? = null,
) : OutputMonitor<T, P> {
    private val headAttached = !GraphicsEnvironment.isHeadless()
    private val maxUiFps = MAX_UI_FPS
    private val initialUiFps = targetFps.coerceIn(MIN_UI_FPS, maxUiFps)
    private val windowStarted = AtomicBoolean(false)

    // Numbers, as simulation files give whole numbers as integers.
    private val quickJumps = QuickJumps(timeJumps.map(Number::toDouble), stepJumps?.map(Number::toLong))
    private val frames = FrameSchedule()
    private val throttle = SimulationThrottle()
    private val currentUiState by lazy {
        ComposeUiStateStore(
            AlchemistUiState(
                controls = SimulationControlsState(
                    uiFps = initialUiFps,
                    fpsInput = initialUiFps.toString(),
                    maxUiFps = maxUiFps,
                    quickJumps = quickJumps,
                ),
            ),
        )
    }

    init {
        check(headAttached || !failOnHeadless) {
            "Cannot show the Compose UI on a JVM without a display. " +
                "To run the simulation without the UI, pass `failOnHeadless: false` to the monitor."
        }
        if (!headAttached) {
            logger.warn(
                "Compose UI requested, but this JVM has no display: the simulation runs without the UI. " +
                    "To fail instead, pass `failOnHeadless: true` to the monitor.",
            )
        }
    }

    override fun initialized(environment: Environment<T, P>) {
        if (headAttached) {
            throttle.reset()
            ensureWindow(environment)
            currentUiState.update {
                it.copy(
                    controls = it.controls.copy(
                        status = environment.simulation.toSimulationStatus(),
                        uiFps = it.controls.uiFps.coerceIn(MIN_UI_FPS, maxUiFps),
                        fpsInput = it.controls.uiFps.coerceIn(MIN_UI_FPS, maxUiFps).toString(),
                        maxUiFps = maxUiFps,
                    ),
                )
            }
            // The simulation starts paused: show its nodes now, not only after the first step.
            val simulation = environment.simulation
            updateUiState(environment, simulation.currentTime, simulation.step)
        }
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        if (headAttached) {
            val controls = currentUiState.state.controls
            val dueNs = throttle.dueAt(controls.pacing, time.toDouble())
            val now = System.nanoTime()
            if (frames.isDue(now, idleNs = dueNs - now, fps = controls.uiFps)) {
                updateUiState(environment, time, step)
                frames.taken(startNs = now, endNs = System.nanoTime(), fps = controls.uiFps)
            }
            throttle.sleepUntil(dueNs)
        }
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        if (headAttached) {
            throttle.wakeUp()
            updateUiState(environment, time, step)
        }
    }

    private fun updateUiState(environment: Environment<T, P>, time: Time, step: Long) {
        // Built once, outside of update, which may run its lambda again if the UI changes the state meanwhile.
        val current = currentUiState.state
        val viewport = environment.toViewport(
            renderLinks = current.scene.showLinks,
            inspected = current.selectedNodeIds,
        )
        currentUiState.update {
            it.copy(
                scene = viewport.copy(showLinks = it.scene.showLinks),
                controls = it.controls.copy(
                    time = time.toDouble(),
                    step = step,
                    status = environment.simulation.toSimulationStatus(),
                ),
            ).withSelection(it.selectedNodeIds)
        }
    }

    private fun ensureWindow(environment: Environment<T, P>) {
        if (windowStarted.compareAndSet(false, true)) {
            Thread {
                val screenSize = Toolkit.getDefaultToolkit().screenSize
                application {
                    val close = {
                        throttle.wakeUp()
                        environment.simulation.takeUnless { it.state == SimulationState.TERMINATED }?.terminate()
                        exitApplication()
                    }
                    Window(
                        onCloseRequest = close,
                        onPreviewKeyEvent = { event ->
                            event.isQuitShortcut.also { quit -> if (quit) close() }
                        },
                        title = "Alchemist",
                        icon = painterResource(Res.drawable.alchemist_logo),
                        state = rememberWindowState(
                            size = DpSize(
                                width = (screenSize.width * 3 / 4).dp,
                                height = (screenSize.height * 3 / 4).dp,
                            ),
                        ),
                    ) {
                        app(
                            remember {
                                engineController(environment)
                            },
                        )
                    }
                }
            }.apply {
                isDaemon = true
                name = "Alchemist Compose UI"
            }.start()
        }
    }

    private fun engineController(environment: Environment<T, P>): ComposeUiController = ComposeUiController(
        currentUiState,
        EngineUiCallbacks(environment.simulation, currentUiState, throttle),
    )

    private companion object {
        private val logger = LoggerFactory.getLogger(ComposeMonitor::class.java)

        /** Whether this is Ctrl+Q being pressed. */
        private val KeyEvent.isQuitShortcut: Boolean
            get() = type == KeyEventType.KeyDown && isCtrlPressed && key == Key.Q
    }
}
