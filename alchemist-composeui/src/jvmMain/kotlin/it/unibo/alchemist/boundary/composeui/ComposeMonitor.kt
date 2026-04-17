/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Monitor extension that uses JVM Compose UI to display the simulation.
 */
class ComposeMonitor<T, P : Position<P>> : OutputMonitor<T, P> {
    private val windowStarted = AtomicBoolean(false)

    override fun initialized(environment: Environment<T, P>) {
        ensureWindow()
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) = Unit

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) = Unit

    private fun ensureWindow() {
        if (windowStarted.compareAndSet(false, true)) {
            application {
                Window(
                    onCloseRequest = { exitApplication() },
                    title = "Alchemist",
                ) {
                    app()
                }
            }
        }
    }
}
