/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.terminators

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import org.kaikikm.threadresloader.ResourceLoader

class TestAfterTime : FreeSpec(
    {
        "simulation with AfterTime terminator should end" {
            val simulation: Simulation<Nothing, Nothing> =
                LoadAlchemist.from(ResourceLoader.getResource("termination.yml")).getDefault()
            simulation.addOutputMonitor(object : OutputMonitor<Nothing, Nothing> {
                override fun finished(environment: Environment<Nothing, Nothing>, time: Time, step: Long) = Unit
                override fun initialized(environment: Environment<Nothing, Nothing>) = Unit
                override fun stepDone(
                    environment: Environment<Nothing, Nothing>,
                    reaction: Actionable<Nothing>?,
                    time: Time,
                    step: Long,
                ) {
                    time.toDouble() shouldBeLessThan 2.0
                }
            })
            simulation.runInCurrentThread()
            simulation.error.ifPresent { throw it }
        }
    },
)
