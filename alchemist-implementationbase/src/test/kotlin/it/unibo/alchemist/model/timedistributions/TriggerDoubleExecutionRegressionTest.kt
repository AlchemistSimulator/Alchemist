/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import io.kotest.core.spec.style.FreeSpec
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import org.kaikikm.threadresloader.ResourceLoader

class TriggerDoubleExecutionRegressionTest : FreeSpec({
    fun startAlchemistFromResource(resource: String) {
        val simulation: Simulation<Nothing, Nothing> =
            LoadAlchemist.from(ResourceLoader.getResource(resource)).getDefault()
        simulation.runInCurrentThread()
        simulation.error.ifPresent { throw it }
    }
    "a trigger should not be executed twice" {
        startAlchemistFromResource("triggerDoubleExecution.yml")
    }
    "two independent triggers, scheduled in different times, should execute once each" {
        startAlchemistFromResource("multipleIndependentTriggers.yml")
    }
    "a single triger with other actions, should execute once" {
        startAlchemistFromResource("singleTriggerAndOtherActions.yml")
    }
})
