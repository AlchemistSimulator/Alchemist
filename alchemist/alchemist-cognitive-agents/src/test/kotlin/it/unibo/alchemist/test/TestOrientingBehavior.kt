package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position2D

class TestOrientingBehavior<T, P : Position2D<P>> : StringSpec({

    /*
     * Runs the specified simulation for the specified number of steps. At the end, it
     * checks that the distance of each pedestrian from the target position specified
     * with coords is less than the specified tolerance.
     */
    fun runSimulation(simulation: String, tolerance: Double, numSteps: Long, vararg coords: Number) {
        loadYamlSimulation<T, P>(simulation).startSimulation(
            finished = { e, _, _ ->
                e.nodes
                    .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
                    .forEach { p ->
                        e.getPosition(p).getDistanceTo(e.makePosition(*coords)) shouldBeLessThan tolerance
                    }
            },
            numSteps = numSteps
        )
    }

    "pedestrian should take nearest door when no spatial info is available" {
        runSimulation("nearest-door.yml", 2.0, 11, 103.0, 99.0)
    }

    "pedestrian with complete knowledge should reach destination" {
        runSimulation("complete-knowledge.yml", 5.0, 120, 135.0, 15.0)
    }

    "pedestrian with partial knowledge (30%) should reach destination" {
        runSimulation("partial-knowledge.yml", 5.0, 170, 135.0, 15.0)
    }

    "pedestrian with no knowledge should reach destination" {
        runSimulation("no-knowledge.yml", 5.0, 250, 135.0, 105.0)
    }

    "pedestrian should avoid congestion" {
        runSimulation("congestion-avoidance.yml", 2.0, 32, 33.0, 33.0)
    }

    "every orienting pedestrian should reach the destination" {
        runSimulation("multiple-orienting-pedestrians.yml", 15.0, 25000, 12.0, 60.0)
    }
})
