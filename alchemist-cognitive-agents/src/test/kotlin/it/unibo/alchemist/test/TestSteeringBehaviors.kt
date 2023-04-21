package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.properties.SocialProperty
import it.unibo.alchemist.testsupport.loadYamlSimulation
import it.unibo.alchemist.testsupport.startSimulation
import kotlin.math.abs

private const val EPSILON = 0.001

class TestSteeringBehaviors<T, P> : StringSpec({

    val filterSocialNode: (Node<T>) -> Boolean = { it.asPropertyOrNull<T, SocialProperty<T>>() != null }

    "nodes seeking a target must approach it" {
        val startDistances = mutableMapOf<Node<T>, Double>()
        val endDistances = mutableMapOf<Node<T>, Double>()
        loadYamlSimulation<T, P>("seek.yml").startSimulation(
            onceInitialized = { environment ->
                environment.nodes.forEach {
                    startDistances[it] = environment.getPosition(it).distanceTo(environment.origin)
                }
            },
            whenFinished = { environment, _, _ ->
                environment.nodes.forEach {
                    endDistances[it] = environment.getPosition(it).distanceTo(environment.origin)
                }
            },
        ).nodes.forEach { startDistances[it]!! shouldBeGreaterThan endDistances[it]!! }
    }

    "nodes fleeing from a target must go away from it" {
        val startDistances = mutableMapOf<Node<T>, Double>()
        val endDistances = mutableMapOf<Node<T>, Double>()
        loadYamlSimulation<T, P>("flee.yml").startSimulation(
            onceInitialized = { e ->
                e.nodes.forEach {
                    startDistances[it] = e.getPosition(it).distanceTo(e.origin)
                }
            },
            whenFinished = { e, _, _ ->
                e.nodes.forEach {
                    endDistances[it] = e.getPosition(it).distanceTo(e.origin)
                }
            },
        ).nodes.forEach { startDistances[it]!! shouldBeLessThan endDistances[it]!! }
    }

    "nodes arriving to a target must decelerate while approaching it" {
        with(loadYamlSimulation<T, P>("arrive.yml")) {
            val nodesPositions: Map<Node<T>, MutableList<P>> = nodes.map { it to mutableListOf<P>() }.toMap()
            startSimulation(
                atEachStep = { e, _, _, _ ->
                    e.nodes.forEach {
                        nodesPositions[it]?.add(e.getPosition(it))
                    }
                },
            )
            nodesPositions.values.forEach { list ->
                val paired = list.asSequence()
                    .zipWithNext()
                    .filter { it.first != it.second }
                    .map { it.first.distanceTo(it.second) }
                    .toList()
                val comparator = { d1: Double, d2: Double ->
                    when {
                        abs(d2 - d1) < EPSILON -> 0
                        d2 > d1 -> 1
                        else -> -1
                    }
                }
                paired shouldBeSortedWith comparator
            }
        }
    }

    "cohesion gives importance to the other members of the group during an evacuation" {
        loadYamlSimulation<T, P>("cohesion.yml").startSimulation(
            whenFinished = { e, _, _ ->
                e.nodes.asSequence()
                    .filter(filterSocialNode)
                    .groupBy { it.asProperty<T, SocialProperty<T>>().group }
                    .values
                    .forEach {
                        for (nodePos in it.map { node -> e.getPosition(node) }) {
                            for (otherPos in (it.map { node -> e.getPosition(node) }.minusElement(nodePos))) {
                                nodePos.distanceTo(otherPos) shouldBeLessThan 4.0
                            }
                        }
                    }
            },
            steps = 20000,
        )
    }

    "nodes using separation behavior keep a distance to each other" {
        loadYamlSimulation<T, P>("separation.yml").startSimulation(
            whenFinished = { e, _, _ ->
                with(e.nodes.map { e.getPosition(it) }) {
                    for (nodePos in this) {
                        for (otherPos in (this.minusElement(nodePos))) {
                            nodePos.distanceTo(otherPos) shouldBeGreaterThan 6.0
                        }
                    }
                }
            },
            steps = 30000,
        )
    }

    "obstacle avoidance let nodes reach destinations behind obstacles" {
        loadYamlSimulation<T, P>("obstacle-avoidance.yml").startSimulation(
            whenFinished = { e, _, _ ->
                e.nodes.forEach {
                    e.getPosition(it).distanceTo(e.makePosition(600.0, 240.0)) shouldBeLessThan 10.0
                }
            },
            steps = 26000,
        )
    }
}) where P : Position2D<P>, P : Vector2D<P>
