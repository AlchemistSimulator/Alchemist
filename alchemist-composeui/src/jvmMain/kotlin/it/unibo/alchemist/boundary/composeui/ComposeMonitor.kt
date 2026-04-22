/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
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
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.adapter.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.adapter.toViewport
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.FullThrottle
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.app
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Monitor extension that uses JVM Compose UI to display the simulation.
 * @param targetFps The initial target frames per second for the UI updates. Defaults to 30.
 */
class ComposeMonitor<T, P : Position<P>> @JvmOverloads constructor(targetFps: Int = 30) : OutputMonitor<T, P> {
    private val maxUiFps = detectMonitorRefreshRate() ?: DEFAULT_MAX_UI_FPS
    private val initialUiFps = targetFps.coerceIn(MIN_UI_FPS, maxUiFps)
    private val windowStarted = AtomicBoolean(false)
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

    override fun initialized(environment: Environment<T, P>) {
        nextEventReleaseNs = 0L
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

    private var lastUpdate: Long = 0L
    private var nextEventReleaseNs: Long = 0L

    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        val now = System.currentTimeMillis()
        if (now - lastUpdate >= uiThrottleMs()) {
            lastUpdate = now
            updateUiState(environment, time, step)
        }
        throttleSimulation()
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        nextEventReleaseNs = 0L
        updateUiState(environment, time, step)
    }

    private fun updateUiState(environment: Environment<T, P>, time: Time, step: Long) {
        currentUiState.update {
            val viewport = environment.toViewport()
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

    private fun uiThrottleMs(): Long =
        (1000.0 / currentUiState.state.controls.uiFps.coerceIn(MIN_UI_FPS, maxUiFps))
            .roundToLong()
            .coerceAtLeast(1L)

    private fun throttleSimulation() {
        val controls = currentUiState.state.controls
        val eventsPerSecond = when (val throttling = controls.simulationEventThrottling) {
            is FullThrottle -> {
                nextEventReleaseNs = 0
                return
            }
            else -> throttling.value
        }
        val intervalNs = (1_000_000_000.0 / eventsPerSecond).roundToLong().coerceAtLeast(1L)
        val now = System.nanoTime()
        nextEventReleaseNs = max(now, nextEventReleaseNs) + intervalNs
        val remainingNs = nextEventReleaseNs - System.nanoTime()
        if (remainingNs > 0L) {
            try {
                TimeUnit.NANOSECONDS.sleep(remainingNs)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun ensureWindow(environment: Environment<T, P>) {
        if (windowStarted.compareAndSet(false, true)) {
            Thread {
                val screenSize = Toolkit.getDefaultToolkit().screenSize
                application {
                    Window(
                        onCloseRequest = { exitApplication() },
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

    private fun alchemistDesktopController(environment: Environment<T, P>): ComposeUiController =
        ComposeUiController(currentUiState, DesktopAlchemistUiCallback(environment.simulation, currentUiState))
}

private fun Time.toComposeUiLabel(): String = toDouble().formatFixed(DISPLAYED_TIME_DECIMALS)

private fun detectMonitorRefreshRate(): Int? = runCatching {
    GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .defaultScreenDevice
        .displayMode
        .refreshRate
        .takeIf { it > 0 }
}.getOrNull()
