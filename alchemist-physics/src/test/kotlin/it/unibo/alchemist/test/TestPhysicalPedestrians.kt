package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector

class TestPhysicalPedestrians<T, P> : StringSpec({
    "node pushes away a node in the seeking target" {
        loadYamlSimulation<T, P>("pushing_behavior.yml").startSimulation(
            steps = 35000,
            whenFinished = { environment, _, _ ->
                environment.getPosition(environment.nodes.first()) shouldNotBe environment.makePosition(0, 0)
            },
        )
    }
}) where P : Position<P>, P : Vector<P>
