package it.unibo.alchemist.agents.test

import io.kotlintest.matchers.doubles.shouldBeGreaterThan
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.agents.cognitive.CognitivePedestrian2D
import it.unibo.alchemist.model.interfaces.Position

class TestFeelsTransmission<T, P : Position<P>> : StringSpec({

    "danger layer affects cognitive pedestrians" {
        val aggregateDangerWithoutLayer = loadYamlSimulation<T, P>("feels-transmission-without-layer.yml")
                .nodes.map { it as CognitivePedestrian2D<T, *> }.sumByDouble { it.fear() }
        val aggregateDangerWithLayer = loadYamlSimulation<T, P>("feels-transmission-with-layer.yml")
                .nodes.map { it as CognitivePedestrian2D<T, *> }.sumByDouble { it.fear() }
        println("Without layer: $aggregateDangerWithoutLayer")
        println("With layer: $aggregateDangerWithLayer")
        aggregateDangerWithLayer shouldBeGreaterThan aggregateDangerWithoutLayer
    }
})