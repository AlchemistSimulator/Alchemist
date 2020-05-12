package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph

/*
 * Tests contained here are dependent on the pedestrian's speed.
 */
class TestOrientingBehavior<T, P : Position2D<P>> : StringSpec({

    /**
     * Asserts that the distance of each pedestrian from the target position specified
     * with [coords] is less than the given [tolerance].
     */
    fun <T, P : Position2D<P>> assertPedestriansReached(
        env: Environment<T, P>,
        tolerance: Double,
        vararg coords: Number
    ) {
        val target = env.makePosition(*coords)
        env.nodes
            .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
            .forEach { p ->
                env.getPosition(p).distanceTo(target) shouldBeLessThan tolerance
            }
    }

    /**
     * Runs the [simulation] for the specified number of steps ([numSteps]). At the end,
     * asserts that the distance of each pedestrian from the target position specified
     * with [coords] is less than the given [tolerance].
     */
    fun runSimulation(simulation: String, tolerance: Double, numSteps: Long, vararg coords: Number) {
        loadYamlSimulation<T, P>(simulation).startSimulation(
            finished = { env, _, _ -> assertPedestriansReached(env, tolerance, *coords) },
            numSteps = numSteps
        )
    }

    "exploring behavior keeps moving the pedestrian indefinitely" {
        /*
         * On a stable version of the behavior it was observed that after 30k steps of execution the
         * pedestrian was in the position specified with coords. This test (which aims to verify that
         * the pedestrian doesn't stop moving) is slightly weak as he/she could stop moving exactly
         * in that position and this test would pass.
         */
        runSimulation(
            "explore.yml",
            0.1,
            30000,
            55.47950927581728, 74.34535994791692
        )
    }

    "goal oriented exploring allows to reach the destination" {
        runSimulation("goal-oriented-explore.yml", 1.0, 850, 135, 15)
    }

    "pursuing allows to reach the destination quicker" {
        runSimulation("pursue.yml", 1.0, 360, 135, 15)
    }

    "route following allows cuts to the route" {
        loadYamlSimulation<T, P>("follow-route.yml").startSimulation(
            stepDone = { env: Environment<T, P>, _, _, _ ->
                if (env is Euclidean2DEnvironmentWithGraph<*, T, *, *>) {
                    val pedestrian = env.nodes.first()
                    val waypointToSkip = env.makePosition(70, 105)
                    env.graph.nodeContaining(waypointToSkip)?.contains(env.getPosition(pedestrian)) shouldBe false
                }
            },
            finished = { env, _, _ -> assertPedestriansReached(env, 1.0, 85, 80) },
            numSteps = 185
        )
    }

    "pedestrian with no knowledge should explore and reach the destination" {
        /*
         * Behavior should be identical to goal oriented exploring.
         */
        runSimulation("no-knowledge.yml", 1.0, 850, 135, 15)
    }

    "pedestrian with complete knowledge should take best route to destination" {
        /*
         * Only taking the best route the destination can be reached in 320 steps.
         */
        runSimulation("complete-knowledge.yml", 1.0, 320, 135, 15)
    }

    "destination reaching should obtain a route from the pedestrian's cognitive map and use it" {
        runSimulation("partial-knowledge.yml", 1.0, 320, 135, 15)
    }

    "destination reaching behavior should allow to reach an unknown destination found along the way to a known one" {
        runSimulation("reach-destination.yml", 1.0, 100, 60, 40)
    }

    "pedestrian should take nearest door when no spatial info is available" {
        runSimulation("nearest-door.yml", 1.0, 40, 103, 99)
    }

    "pedestrian should avoid congestion" {
        loadYamlSimulation<T, P>("congestion-avoidance.yml").startSimulation(
            stepDone = { env: Environment<T, P>, _, _, _ ->
                if (env is Euclidean2DEnvironmentWithGraph<*, T, *, *>) {
                    val pedestrian = env.nodes.first()
                    val roomToAvoid = env.graph.nodeContaining(env.makePosition(40, 40))
                    roomToAvoid?.contains(env.getPosition(pedestrian)) shouldBe false
                }
            },
            finished = { env, _, _ -> assertPedestriansReached(env, 1.0, 10, 55) },
            numSteps = 70
        )
    }
})
