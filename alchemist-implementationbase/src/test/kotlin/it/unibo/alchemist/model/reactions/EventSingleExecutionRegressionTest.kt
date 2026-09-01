/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import io.kotest.core.spec.style.FreeSpec
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import kotlin.test.assertFalse
import kotlin.test.assertIs
import org.kaikikm.threadresloader.ResourceLoader

class EventSingleExecutionRegressionTest : FreeSpec({
    fun startAlchemistFromResource(resource: String) {
        val simulation: Simulation<Nothing, Nothing> =
            LoadAlchemist.from(ResourceLoader.getResource(resource)).getDefault()
        simulation.runInCurrentThread()
        simulation.error.ifPresent { throw it }
    }
    "YAML constructs an event that owns no time distribution" {
        val simulation: Simulation<Nothing, Nothing> =
            LoadAlchemist.from(ResourceLoader.getResource("eventSingleExecution.yml")).getDefault()
        val event = simulation.environment.nodes.first().reactions.single()

        assertIs<AbsoluteEvent<*>>(event)
        assertFalse(TimeDistributedReaction::class.java.isInstance(event))
    }
    "an event should not be executed twice" {
        startAlchemistFromResource("eventSingleExecution.yml")
    }
    "a conditional event should not be executed twice" {
        startAlchemistFromResource("conditionalEventSingleExecution.yml")
    }
    "two independent events, scheduled at different times, should execute once each" {
        startAlchemistFromResource("multipleIndependentEvents.yml")
    }
    "a single event with recurring reactions should execute once" {
        startAlchemistFromResource("singleEventAndOtherReactions.yml")
    }
})
