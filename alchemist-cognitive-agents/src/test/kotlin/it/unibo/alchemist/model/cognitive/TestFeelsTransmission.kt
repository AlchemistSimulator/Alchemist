/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.test.loadYamlSimulation
import it.unibo.alchemist.test.startSimulation

class TestFeelsTransmission<T> : StringSpec({

    "danger layer affects cognitive pedestrians" {
        fun Environment<T, Euclidean2DPosition>.perceivedDanger() = nodes
            .mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.cognitiveModel }
            .sumOf { it.dangerBelief() }
        fun Simulation<T, Euclidean2DPosition>.dangerIsLoaded() = this.also { _ ->
            environment.nodes.mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.danger }
                .forEach { it shouldNotBe null }
        }
        val aggregateDangerWithLayer = loadYamlSimulation<T, Euclidean2DPosition>("feels-transmission-with-layer.yml")
            .also { it.environment.layers shouldNot beEmpty() }
            .dangerIsLoaded()
            .startSimulation()
        val aggregateDangerWithoutLayer = loadYamlSimulation<T, Euclidean2DPosition>("feels-transmission-without-layer.yml")
            .also { it.getEnvironment().layers should beEmpty() }
            .startSimulation()
        println("Without layer aggregate danger: $aggregateDangerWithoutLayer")
        println("With layer aggregate danger: $aggregateDangerWithLayer")
        aggregateDangerWithLayer.perceivedDanger() shouldBeGreaterThan aggregateDangerWithoutLayer.perceivedDanger()
    }

    "social contagion makes nodes evacuate despite they haven't directly seen the danger" {
        loadYamlSimulation<T, Euclidean2DPosition>("social-contagion.yml").startSimulation(
            steps = 8000,
            whenFinished = { environment, _, _ ->
                val reference = environment.makePosition(-50.0, 0.0)
                environment.nodes.forEach {
                    environment.getPosition(it).distanceTo(reference) shouldBeLessThan 13.0
                }
            },
        )
    }
})
