/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.test.loadYamlSimulation
import it.unibo.alchemist.test.startSimulation
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestFeelsTransmission<T> {
    private fun Environment<T, Euclidean2DPosition>.perceivedDanger(): Double = nodes
        .mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.cognitiveModel }
        .sumOf { it.dangerBelief() }

    private fun Simulation<T, Euclidean2DPosition>.dangerIsLoaded(): Simulation<T, Euclidean2DPosition> = apply {
        environment.nodes
            .mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.danger }
            .forEach { assertNotNull(it, "Danger property should be loaded") }
    }

    @Test
    fun `Danger layer affects cognitive pedestrians`() {
        val aggregateDangerWithLayer =
            loadYamlSimulation<T, Euclidean2DPosition>("feels-transmission-with-layer.yml")
                .also { assertFalse(it.environment.layers.isEmpty(), "Expected non-empty layers") }
                .dangerIsLoaded()
                .startSimulation()

        val aggregateDangerWithoutLayer =
            loadYamlSimulation<T, Euclidean2DPosition>("feels-transmission-without-layer.yml")
                .also { assertTrue(it.getEnvironment().layers.isEmpty(), "Expected empty layers") }
                .startSimulation()

        println("Without layer aggregate danger: ${aggregateDangerWithoutLayer.perceivedDanger()}")
        println("With layer aggregate danger: ${aggregateDangerWithLayer.perceivedDanger()}")

        assertTrue(
            aggregateDangerWithLayer.perceivedDanger() > aggregateDangerWithoutLayer.perceivedDanger(),
            "Danger perception should be higher with a danger layer",
        )
    }

    @Test
    fun `Social contagion makes nodes evacuate despite they haven't directly seen the danger`() {
        loadYamlSimulation<T, Euclidean2DPosition>("social-contagion.yml").startSimulation(
            steps = 8000,
            whenFinished = { environment, _, _ ->
                val reference = environment.makePosition(-50.0, 0.0)
                environment.nodes.forEach {
                    assertTrue(
                        environment.getPosition(it).distanceTo(reference) < 13.0,
                        "Node should have moved closer to the evacuation reference point",
                    )
                }
            },
        )
    }
}
