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
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldNotBeIn
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.cognitiveagents.NavigationAction
import it.unibo.alchemist.model.cognitiveagents.NavigationStrategy
import it.unibo.alchemist.model.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.OrientingProperty
import it.unibo.alchemist.testsupport.loadYamlSimulation
import it.unibo.alchemist.testsupport.startSimulation
import org.apache.commons.collections4.queue.CircularFifoQueue

/**
 * Contains tests concerning [NavigationAction]s and [NavigationStrategy], such tests are
 * dependent on the pedestrian's speed.
 */
class TestOrientingBehavior<T, P> : StringSpec({

    fun Iterable<Node<T>>.orienting() = filter { it.asPropertyOrNull<T, OrientingProperty<T, P, *, *, *, *>>() != null }

    /**
     * Asserts that the distance of each pedestrian from the target position specified
     * with [coords] is less than the given [tolerance].
     */
    fun assertPedestriansReached(
        environment: Environment<T, P>,
        tolerance: Double,
        vararg coords: Number,
    ) {
        val target = environment.makePosition(*coords)
        environment.nodes
            .orienting()
            .forEach { p -> environment.getPosition(p).distanceTo(target) shouldBeLessThan tolerance }
    }

    /**
     * Runs the [simulation] for the specified number of steps ([steps]). At the end,
     * asserts that the distance of each pedestrian from the target position specified
     * with [coords] is less than the given [tolerance].
     */
    fun runSimulation(
        simulation: String,
        tolerance: Double,
        steps: Long,
        vararg coords: Number,
    ) {
        loadYamlSimulation<T, P>(simulation).startSimulation(
            onceInitialized = { it.nodes shouldNot beEmpty() },
            whenFinished = { environment, _, _ -> assertPedestriansReached(environment, tolerance, *coords) },
            steps = steps,
        )
    }

    "exploring behavior keeps moving the pedestrian indefinitely" {
        val expectedSize = 2
        val previousPositions: MutableCollection<P?> = CircularFifoQueue(expectedSize)
        loadYamlSimulation<T, P>("explore.yml").startSimulation(
            steps = 3000,
            onceInitialized = { it.nodes.size shouldBe 1 },
            atEachStep = { environment: Environment<T, P>, _, _, _ ->
                val currentPosition = environment.getPosition(environment.nodes.first())
                previousPositions.add(currentPosition)
                if (previousPositions.size == expectedSize) {
                    previousPositions.distinct() shouldNotBe 1
                }
            },
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
            atEachStep = { environment: Environment<T, P>, _, _, _ ->
                if (environment is Euclidean2DEnvironmentWithGraph<*, T, *, *>) {
                    val node = environment.nodes.first()
                    val waypointToSkip = environment.makePosition(70, 105)
                    environment.getPosition(node).shouldNotBeIn(environment.graph.nodeContaining(waypointToSkip))
                }
            },
            whenFinished = { environment, _, _ -> assertPedestriansReached(environment, 1.0, 85, 80) },
            steps = 190,
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
        runSimulation("partial-knowledge.yml", 1.0, 500, 135, 15)
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
            atEachStep = { environment: Environment<T, P>, _, _, _ ->
                if (environment is Euclidean2DEnvironmentWithGraph<*, T, *, *> && !corridorTaken) {
                    val node = environment.nodes.orienting().first()
                    val corridorToTake = environment.graph.nodeContaining(environment.makePosition(35.0, 31.0))
                    corridorTaken = corridorToTake?.contains(environment.getPosition(node)) ?: false
                }
            },
            whenFinished = { environment, _, _ ->
                assertPedestriansReached(environment, 1.0, 10, 55)
                corridorTaken shouldBe true
            },
            steps = 70,
        )
    }
}) where P : Position<P>, P : Vector<P>
