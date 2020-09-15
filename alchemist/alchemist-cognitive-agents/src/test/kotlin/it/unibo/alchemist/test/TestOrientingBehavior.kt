/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.Vector
import loadYamlSimulation
import startSimulation

/**
 * Contains tests concerning [NavigationAction]s and [NavigationStrategy], such tests are
 * dependent on the pedestrian's speed.
 */
class TestOrientingBehavior<T, P> : StringSpec({

    /**
     * Asserts that the distance of each pedestrian from the target position specified
     * with [coords] is less than the given [tolerance].
     */
    fun assertPedestriansReached(
        env: Environment<T, P>,
        tolerance: Double,
        vararg coords: Number
    ) {
        val target = env.makePosition(*coords)
        env.nodes
            .filterIsInstance<OrientingPedestrian<T, *, *, *, *>>()
            .forEach { p -> env.getPosition(p).distanceTo(target) shouldBeLessThan tolerance }
    }

    /**
     * Runs the [simulation] for the specified number of steps ([steps]). At the end,
     * asserts that the distance of each pedestrian from the target position specified
     * with [coords] is less than the given [tolerance].
     */
    fun runSimulation(simulation: String, tolerance: Double, steps: Long, vararg coords: Number) {
        loadYamlSimulation<T, P>(simulation).startSimulation(
            finished = { env, _, _ -> assertPedestriansReached(env, tolerance, *coords) },
            steps = steps
        )
    }

    "exploring behavior keeps moving the pedestrian indefinitely" {
        /*
         * On a stable version of the behavior it was observed that after 3k steps of execution the
         * pedestrian was in the position specified with coords. This test (which aims to verify that
         * the pedestrian doesn't stop moving) is slightly weak as he/she could stop moving exactly
         * in that position and this test would pass.
         */
        runSimulation(
            "explore.yml",
            0.1,
            3000,
            44.910744827515124, 19.554979285729484
        )
    }

    "goal oriented exploring allows to reach the destination" {
        runSimulation("goal-oriented-explore.yml", 1.0, 900, 135, 15)
    }

    "pursuing allows to reach the destination quicker" {
        runSimulation("pursue.yml", 1.0, 370, 135, 15)
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
            steps = 190
        )
    }

    "pedestrian with no knowledge should explore and reach the destination" {
        /*
         * Behavior should be identical to goal oriented exploring.
         */
        runSimulation("no-knowledge.yml", 1.0, 900, 135, 15)
    }

    "pedestrian with complete knowledge should take best route to destination" {
        /*
         * Only taking the best route the destination can be reached in 320 steps.
         */
        runSimulation("complete-knowledge.yml", 1.0, 330, 135, 15)
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
        var corridorTaken = false
        loadYamlSimulation<T, P>("congestion-avoidance.yml").startSimulation(
            stepDone = { env: Environment<T, P>, _, _, _ ->
                if (env is Euclidean2DEnvironmentWithGraph<*, T, *, *> && !corridorTaken) {
                    val pedestrian = env.nodes.filterIsInstance<OrientingPedestrian<T, *, *, *, *>>().first()
                    val corridorToTake = env.graph.nodeContaining(env.makePosition(35.0, 31.0))
                    corridorTaken = corridorToTake?.contains(env.getPosition(pedestrian)) ?: false
                }
            },
            finished = { env, _, _ ->
                assertPedestriansReached(env, 1.0, 10, 55)
                corridorTaken shouldBe true
            },
            steps = 70
        )
    }
}) where P : Position<P>, P : Vector<P>
