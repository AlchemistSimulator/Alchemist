package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import it.unibo.alchemist.model.cognitiveagents.CognitiveAgent
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.Vector
import loadYamlSimulation
import startSimulation

class TestFeelsTransmission<T, P> : StringSpec({

    "danger layer affects cognitive pedestrians" {
        val aggregateDangerWithoutLayer = loadYamlSimulation<T, P>("feels-transmission-without-layer.yml")
                .startSimulation()
                .nodes
                .map { it as CognitiveAgent }
                .sumByDouble { it.cognitive.dangerBelief() }
        val aggregateDangerWithLayer = loadYamlSimulation<T, P>("feels-transmission-with-layer.yml")
                .startSimulation()
                .nodes
                .map { it as CognitiveAgent }
                .sumByDouble { it.cognitive.dangerBelief() }
        println("Without layer aggregate danger: $aggregateDangerWithoutLayer")
        println("With layer aggregate danger: $aggregateDangerWithLayer")
        aggregateDangerWithLayer shouldBeGreaterThan aggregateDangerWithoutLayer
    }

    "social contagion makes nodes evacuate despite they haven't directly seen the danger" {
        loadYamlSimulation<T, P>("social-contagion.yml").startSimulation(
            steps = 8000,
            finished = { e, _, _ -> e.nodes.forEach {
                e.getPosition(it).distanceTo(e.makePosition(-50.0, 0.0)) shouldBeLessThan 13.0 }
            }
        )
    }
}) where P : Position<P>, P : Vector<P>
