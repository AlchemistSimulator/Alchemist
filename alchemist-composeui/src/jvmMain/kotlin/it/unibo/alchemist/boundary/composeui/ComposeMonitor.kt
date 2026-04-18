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
import it.unibo.alchemist.boundary.composeui.adapter.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.adapter.toViewport
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import java.awt.Toolkit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Monitor extension that uses JVM Compose UI to display the simulation.
 * @param targetFps The target frames per second for the UI updates. Defaults to 30.
 */
class ComposeMonitor<T, P : Position<P>> @JvmOverloads constructor(targetFps: Int = 30) : OutputMonitor<T, P> {
    private val throttleMs = 1000L / targetFps.coerceAtLeast(1)
    private val windowStarted = AtomicBoolean(false)
    private val currentUiState by lazy { ComposeUiStateStore(AlchemistUiState()) }

    override fun initialized(environment: Environment<T, P>) {
        ensureWindow(environment)
        currentUiState.update {
            it.copy(controls = it.controls.copy(status = environment.simulation.toSimulationStatus()))
        }
    }

    private var lastUpdate: Long = 0L

    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        val now = System.currentTimeMillis()
        if (now - lastUpdate >= throttleMs) {
            lastUpdate = now
            updateUiState(environment, time, step)
        }
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
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
            )
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

private const val DISPLAYED_TIME_DECIMALS = 2
