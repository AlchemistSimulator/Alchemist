/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.EuclideanEnvironment
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty
import it.unibo.alchemist.testsupport.loadYamlSimulation
import it.unibo.alchemist.testsupport.startSimulation

class TestFeelsTransmission<T, P> : StringSpec({

    "danger layer affects cognitive pedestrians" {
        fun Environment<T, P>.perceivedDanger() = nodes
            .mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.cognitiveModel }
            .sumOf { it.dangerBelief() }
        fun EuclideanEnvironment<T, P>.dangerIsLoaded() = this.also { _ ->
            nodes.mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>()?.danger }
                .forEach { it shouldNotBe null }
        }
        val aggregateDangerWithLayer = loadYamlSimulation<T, P>("feels-transmission-with-layer.yml")
            .also { it.layers shouldNot beEmpty() }
            .dangerIsLoaded()
            .startSimulation()
        val aggregateDangerWithoutLayer = loadYamlSimulation<T, P>("feels-transmission-without-layer.yml")
            .also { it.layers should beEmpty() }
            .startSimulation()
        println("Without layer aggregate danger: $aggregateDangerWithoutLayer")
        println("With layer aggregate danger: $aggregateDangerWithLayer")
        aggregateDangerWithLayer.perceivedDanger() shouldBeGreaterThan aggregateDangerWithoutLayer.perceivedDanger()
    }

    "social contagion makes nodes evacuate despite they haven't directly seen the danger" {
        loadYamlSimulation<T, P>("social-contagion.yml").startSimulation(
            steps = 8000,
            whenFinished = { e, _, _ ->
                e.nodes.forEach {
                    e.getPosition(it).distanceTo(e.makePosition(-50.0, 0.0)) shouldBeLessThan 13.0
                }
            },
        )
    }
}) where P : Position<P>, P : Vector<P>
