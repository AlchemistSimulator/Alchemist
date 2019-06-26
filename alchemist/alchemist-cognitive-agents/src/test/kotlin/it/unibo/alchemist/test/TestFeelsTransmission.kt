package it.unibo.alchemist.test

import io.kotlintest.matchers.doubles.shouldBeGreaterThan
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.implementations.nodes.CognitivePedestrian2D
import it.unibo.alchemist.model.interfaces.Position

class TestFeelsTransmission<T, P : Position<P>> : StringSpec({

    "danger layer affects cognitive pedestrians" {
        val aggregateDangerWithoutLayer = loadYamlSimulation<T, P>("feels-transmission-without-layer.yml")
                .nodes
                .map { it as CognitivePedestrian2D<T, *> }
                .sumByDouble { it.dangerBelief() }
        val aggregateDangerWithLayer = loadYamlSimulation<T, P>("feels-transmission-with-layer.yml")
                .nodes
                .map { it as CognitivePedestrian2D<T, *> }
                .sumByDouble { it.dangerBelief() }
        println("Without layer aggregate danger: $aggregateDangerWithoutLayer")
        println("With layer aggregate danger: $aggregateDangerWithLayer")
        aggregateDangerWithLayer shouldBeGreaterThan aggregateDangerWithoutLayer
    }
})