package it.unibo.alchemist.test

import io.kotlintest.matchers.doubles.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.interfaces.Position2D

class TestSteeringBehaviors<T, P : Position2D<P>> : StringSpec({

    "seek" {
        loadYamlSimulation<T, P>("seek.yml").startSimulation(
            finished = { e, _, _ -> e.nodes.forEach { e.getPosition(it) shouldBe e.makePosition(0.0, 0.0) } }
        )
    }

    "flee" {
        loadYamlSimulation<T, P>("flee.yml").startSimulation(
            finished = { e, _, _ -> e.nodes.forEach {
                e.getPosition(it).getDistanceTo(e.makePosition(0, 0)) shouldBeGreaterThan 100.0
            } }
        )
    }
})