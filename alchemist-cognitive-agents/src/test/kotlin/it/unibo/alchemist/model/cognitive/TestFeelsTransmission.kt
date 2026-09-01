/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.test.loadYamlSimulation
import it.unibo.alchemist.test.startSimulation
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class TestFeelsTransmission<T> {
    private fun Environment<T, Euclidean2DPosition>.perceivedDanger(): Double = nodes
        .mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.cognitiveModel }
        .sumOf { it.dangerBelief() }

    private fun Simulation<T, Euclidean2DPosition>.dangerIsLoaded(): Simulation<T, Euclidean2DPosition> = apply {
        environment.nodes
            .mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.danger }
            .forEach { assertNotNull(it, "Danger property should be loaded") }
    }

    private fun Environment<T, Euclidean2DPosition>.directDanger(node: Node<T>): Double =
        requireNotNull(getLayer(incarnation.createMolecule("danger"))) { "Danger layer should be loaded" }
            .getValue(getCurrentPosition(node))
            .let { requireNotNull(it as? Number) { "Danger layer should contain numeric values" }.toDouble() }

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
//        println("Without layer aggregate danger: ${aggregateDangerWithoutLayer.perceivedDanger()}")
//        println("With layer aggregate danger: ${aggregateDangerWithLayer.perceivedDanger()}")
        assertTrue(
            aggregateDangerWithLayer.perceivedDanger() > aggregateDangerWithoutLayer.perceivedDanger(),
            "Danger perception should be higher with a danger layer",
        )
    }

    @Test
    fun `Social contagion makes nodes evacuate despite they haven't directly seen the danger`() {
        val maximumSteps = 100L
        lateinit var indirectlyExposed: Node<T>
        lateinit var target: Euclidean2DPosition
        var initialDistanceFromTarget = Double.NaN
        var socialContagionObserved = false
        var evacuationObserved = false
        loadYamlSimulation<T, Euclidean2DPosition>("social-contagion.yml").startSimulation(
            steps = maximumSteps,
            onceInitialized = { environment ->
                val (directlyExposed, unexposed) = environment.nodes.partition { environment.directDanger(it) > 0 }
                assertEquals(1, directlyExposed.size, "The scenario should contain one directly exposed pedestrian")
                assertEquals(1, unexposed.size, "The scenario should contain one unexposed pedestrian")
                indirectlyExposed = unexposed.single()
                assertEquals(0.0, environment.directDanger(indirectlyExposed))
                val source = directlyExposed.single()
                (environment as Physics2DEnvironment<T>).setHeading(
                    indirectlyExposed,
                    environment.getCurrentPosition(source) - environment.getCurrentPosition(indirectlyExposed),
                )
                val sourceModel = requireNotNull(source.asPropertyOrNull<T, CognitiveProperty<T>>()).cognitiveModel
                val indirectCognition = requireNotNull(
                    indirectlyExposed.asPropertyOrNull<T, CognitiveProperty<T>>(),
                )
                assertFalse(
                    indirectCognition.cognitiveModel.wantsToEscape(),
                    "The unexposed pedestrian should not initially want to escape",
                )
                assertEquals(
                    listOf(sourceModel),
                    indirectCognition.influentialPeople(),
                    "The unexposed pedestrian should see the directly exposed pedestrian",
                )
                target = environment.makePosition(-50.0, 0.0)
                initialDistanceFromTarget = environment.getCurrentPosition(indirectlyExposed).distanceTo(target)
            },
            atEachStep = { environment, _, _, _ ->
                assertEquals(
                    0.0,
                    environment.directDanger(indirectlyExposed),
                    "The indirectly exposed pedestrian should never enter the danger region",
                )
                val cognitiveModel = requireNotNull(
                    indirectlyExposed.asPropertyOrNull<T, CognitiveProperty<T>>()?.cognitiveModel,
                )
                socialContagionObserved = socialContagionObserved || cognitiveModel.dangerBelief() > 0
                if (
                    cognitiveModel.wantsToEscape() &&
                    environment.getCurrentPosition(indirectlyExposed).distanceTo(target) < initialDistanceFromTarget
                ) {
                    evacuationObserved = true
                    environment.simulation.terminate()
                }
            },
            whenFinished = { _, _, steps ->
                assertTrue(socialContagionObserved, "The unexposed pedestrian should perceive danger socially")
                assertTrue(evacuationObserved, "The unexposed pedestrian should decide to escape and move")
                assertTrue(steps < maximumSteps, "The regression should complete before its safety step limit")
            },
        )
    }
}
