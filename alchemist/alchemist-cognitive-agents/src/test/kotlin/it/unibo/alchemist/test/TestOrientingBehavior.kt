package it.unibo.alchemist.test

import io.kotlintest.matchers.doubles.shouldBeLessThan
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position2D

class TestOrientingBehavior<T, P : Position2D<P>> : StringSpec({

    "pedestrian should take nearest door when no spatial info is available" {
        loadYamlSimulation<T, P>("nearest-door.yml").startSimulation(
            finished = { e, _, _ ->
                val expectedPos = e.makePosition(103.0, 99.0)
                e.nodes
                    .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
                    .forEach { p ->
                        e.getPosition(p).getDistanceTo(expectedPos) shouldBeLessThan 2.0
                    }
            },
            numSteps = 11
        )
    }

    "pedestrian with complete knowledge should reach destination" {
        loadYamlSimulation<T, P>("complete-knowledge.yml").startSimulation(
            finished = { e, _, _ ->
                val destination = e.makePosition(135.0, 15.0)
                e.nodes
                .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
                .forEach { p ->
                    e.getPosition(p).getDistanceTo(destination) shouldBeLessThan 5.0
                }
            },
            numSteps = 120
        )
    }

    "pedestrian with partial knowledge (30%) should reach destination" {
        loadYamlSimulation<T, P>("partial-knowledge.yml").startSimulation(
            finished = { e, _, _ ->
                val destination = e.makePosition(135.0, 15.0)
                e.nodes
                    .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
                    .forEach { p ->
                        e.getPosition(p).getDistanceTo(destination) shouldBeLessThan 5.0
                    }
            },
            numSteps = 170
        )
    }

    "pedestrian with no knowledge should reach destination" {
        loadYamlSimulation<T, P>("no-knowledge.yml").startSimulation(
            finished = { e, _, _ ->
                val destination = e.makePosition(135.0, 105.0)
                e.nodes
                    .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
                    .forEach { p ->
                        e.getPosition(p).getDistanceTo(destination) shouldBeLessThan 5.0
                    }
            },
            numSteps = 250
        )
    }

    "pedestrian should avoid congestion" {
        loadYamlSimulation<T, P>("congestion-avoidance.yml").startSimulation(
            finished = { e, _, _ ->
                val expectedPos = e.makePosition(33.0, 33.0)
                e.nodes
                    .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
                    .forEach { p ->
                        /*
                         * This is quite strict
                         */
                        e.getPosition(p).getDistanceTo(expectedPos) shouldBeLessThan 2.0
                    }
            },
            numSteps = 32
        )
    }

    "every orienting pedestrian should reach the destination" {
        loadYamlSimulation<T, P>("multiple-orienting-pedestrians.yml").startSimulation(
            finished = { e, _, _ ->
                val expectedPos = e.makePosition(12.0, 60.0)
                e.nodes
                    .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
                    .forEach { p ->
                        e.getPosition(p).getDistanceTo(expectedPos) shouldBeLessThan 15.0
                    }
            },
            numSteps = 25000
        )
    }
})
