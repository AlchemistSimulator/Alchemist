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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DEFAULT_MAX_UI_FPS
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.toViewport
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistSimulations.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.throttling.FullThrottle
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed
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
import kotlin.math.roundToLong
import org.slf4j.LoggerFactory

/**
 * Shows the simulation in a desktop window.
 * Closing the window terminates the simulation, so exporters can finish and Alchemist can exit.
 * On a JVM without a display, it logs a warning and does nothing, unless [failOnHeadless] is true.
 *
 * @param targetFps how many times per second the UI redraws at startup.
 * @param failOnHeadless whether to fail, instead of warning, when there is no display.
 */
class ComposeMonitor<T, P : Position<P>> @JvmOverloads constructor(
    targetFps: Int = 30,
    failOnHeadless: Boolean = false,
) : OutputMonitor<T, P> {
    private val headAttached = !GraphicsEnvironment.isHeadless()
    private val maxUiFps = detectMonitorRefreshRate() ?: DEFAULT_MAX_UI_FPS
    private val initialUiFps = targetFps.coerceIn(MIN_UI_FPS, maxUiFps)
    private val windowStarted = AtomicBoolean(false)
    private var lastUpdate: Long = 0L
    private val throttle = SimulationThrottle()
    private val currentUiState by lazy {
        ComposeUiStateStore(
            AlchemistUiState(
                controls = SimulationControlsState(
                    uiFps = initialUiFps,
                    fpsInput = initialUiFps.toString(),
                    maxUiFps = maxUiFps,
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
        }
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        if (headAttached) {
            val now = System.currentTimeMillis()
            if (now - lastUpdate >= uiThrottleMs()) {
                lastUpdate = now
                updateUiState(environment, time, step)
            }
            throttleSimulation()
        }
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        if (headAttached) {
            throttle.wakeUp()
            updateUiState(environment, time, step)
        }
    }

    private fun updateUiState(environment: Environment<T, P>, time: Time, step: Long) {
        currentUiState.update {
            val viewport = environment.toViewport(renderLinks = it.scene.showLinks)
            val displayedTime = time.toComposeUiLabel()
            it.copy(
                scene = viewport.copy(showLinks = it.scene.showLinks),
                controls = it.controls.copy(
                    timeLabel = displayedTime,
                    step = step,
                    status = environment.simulation.toSimulationStatus(),
                ),
            ).withSelection(it.selectedNodeIds)
        }
    }

    private fun uiThrottleMs(): Long = (1000.0 / currentUiState.state.controls.uiFps.coerceIn(MIN_UI_FPS, maxUiFps))
        .roundToLong()
        .coerceAtLeast(1L)

    private fun throttleSimulation() {
        val throttling = currentUiState.state.controls.simulationEventThrottling
        throttle.await(throttling.takeUnless { it is FullThrottle }?.value)
    }

    private fun ensureWindow(environment: Environment<T, P>) {
        if (windowStarted.compareAndSet(false, true)) {
            Thread {
                val screenSize = Toolkit.getDefaultToolkit().screenSize
                application {
                    Window(
                        onCloseRequest = {
                            throttle.wakeUp()
                            environment.simulation.takeUnless { it.state == SimulationState.TERMINATED }?.terminate()
                            exitApplication()
                        },
                        title = "Alchemist",
                        state = rememberWindowState(
                            size = DpSize(
                                width = (screenSize.width * 3 / 4).dp,
                                height = (screenSize.height * 3 / 4).dp,
                            ),
                        ),
                    ) {
                        app(
                            remember {
                                alchemistDesktopController(environment)
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

    private fun alchemistDesktopController(environment: Environment<T, P>): ComposeUiController = ComposeUiController(
        currentUiState,
        DesktopAlchemistUiCallback(environment.simulation, currentUiState, throttle),
    )

    private companion object {
        private val logger = LoggerFactory.getLogger(ComposeMonitor::class.java)

        private fun Time.toComposeUiLabel(): String = toDouble().formatFixed(DISPLAYED_TIME_DECIMALS)

        private fun detectMonitorRefreshRate(): Int? = runCatching {
            GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .defaultScreenDevice
                .displayMode
                .refreshRate
                .takeIf { it > 0 }
        }.getOrNull()
    }
}
