package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.model.cognitiveagents.CognitiveAgent
import it.unibo.alchemist.model.implementations.nodes.AbstractCognitivePedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EuclideanEnvironment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.Vector
import loadYamlSimulation
import startSimulation

class TestFeelsTransmission<T, P> : StringSpec({

    "danger layer affects cognitive pedestrians" {
        fun Environment<T, P>.perceivedDanger() =
            nodes.filterIsInstance<CognitiveAgent>().sumOf { it.cognitive.dangerBelief() }
        fun EuclideanEnvironment<T, P>.dangerIsLoaded() = this.also {
            nodes.filterIsInstance<AbstractCognitivePedestrian<*, *, *, *>>().forEach {
                it.danger shouldNotBe null
            }
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
            finished = { e, _, _ ->
                e.nodes.forEach {
                    e.getPosition(it).distanceTo(e.makePosition(-50.0, 0.0)) shouldBeLessThan 13.0
                }
            }
        )
    }
}) where P : Position<P>, P : Vector<P>
