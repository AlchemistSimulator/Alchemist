/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import another.location.SimpleMonitor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.times.DoubleTime

class TestMonitorsSyntax<T, P : Position<P>> : FreeSpec(
    {
        "output monitor can be specified via YAML" {
            val initialized = loadAlchemist<T, P>("testmonitors.yml")
            val monitor = initialized.monitors.first()
            val simulation = initialized.createSimulation(finalTime = DoubleTime(1.0))
            simulation.finalStep
            simulation.runInCurrentThread()
            when (monitor) {
                is SimpleMonitor -> {
                    monitor.initialized shouldBe true
                    monitor.finished shouldBe true
                }
                else -> error("Unexpected monitor type")
            }
        }
    },
)
