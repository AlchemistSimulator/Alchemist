package it.unibo.alchemist.test

import io.kotlintest.matchers.doubles.shouldBeGreaterThan
import io.kotlintest.matchers.doubles.shouldBeLessThan
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Position

class TestFeelsTransmission<T, P : Position<P>> : StringSpec({

    "danger layer affects cognitive pedestrians" {
        val aggregateDangerWithoutLayer = loadYamlSimulation<T, P>("feels-transmission-without-layer.yml")
                .startSimulation()
                .nodes
                .map { it as CognitivePedestrian<T> }
                .sumByDouble { it.dangerBelief() }
        val aggregateDangerWithLayer = loadYamlSimulation<T, P>("feels-transmission-with-layer.yml")
                .startSimulation()
                .nodes
                .map { it as CognitivePedestrian<T> }
                .sumByDouble { it.dangerBelief() }
        println("Without layer aggregate danger: $aggregateDangerWithoutLayer")
        println("With layer aggregate danger: $aggregateDangerWithLayer")
        aggregateDangerWithLayer shouldBeGreaterThan aggregateDangerWithoutLayer
    }

    "social contagion makes nodes evacuate despite they haven't directly seen the danger" {
        loadYamlSimulation<T, P>("social-contagion.yml").startSimulation(
            finished = { e, _, _ -> e.nodes.forEach {
                e.getPosition(it).getDistanceTo(e.makePosition(-50.0, 0.0)) shouldBeLessThan 10.0 }
            }
        )
    }
})
