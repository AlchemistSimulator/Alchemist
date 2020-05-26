package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import kotlin.math.abs

private const val EPSILON = 0.001

class TestSteeringBehaviors<T, P : Position2D<P>> : StringSpec({

    "nodes seeking a target must approach it" {
        val startDistances = mutableMapOf<Node<T>, Double>()
        val endDistances = mutableMapOf<Node<T>, Double>()
        loadYamlSimulation<T, P>("seek.yml").startSimulation(
            initialized = { e -> e.nodes.forEach {
                startDistances[it] = e.getPosition(it).distanceTo(e.origin())
            } },
            finished = { e, _, _ -> e.nodes.forEach {
                endDistances[it] = e.getPosition(it).distanceTo(e.origin())
            } }
        ).nodes.forEach { startDistances[it]!! shouldBeGreaterThan endDistances[it]!! }
    }

    "nodes fleeing from a target must go away from it" {
        val startDistances = mutableMapOf<Node<T>, Double>()
        val endDistances = mutableMapOf<Node<T>, Double>()
        loadYamlSimulation<T, P>("flee.yml").startSimulation(
            initialized = { e -> e.nodes.forEach {
                startDistances[it] = e.getPosition(it).distanceTo(e.origin())
            } },
            finished = { e, _, _ -> e.nodes.forEach {
                endDistances[it] = e.getPosition(it).distanceTo(e.origin())
            } }
        ).nodes.forEach { startDistances[it]!! shouldBeLessThan endDistances[it]!! }
    }

    "nodes arriving to a target must decelerate while approaching it" {
        with(loadYamlSimulation<T, P>("arrive.yml")) {
            val nodesPositions: Map<Node<T>, MutableList<P>> = nodes.map { it to mutableListOf<P>() }.toMap()
            startSimulation(
                stepDone = { e, _, _, _ -> e.nodes.forEach {
                    nodesPositions[it]?.add(e.getPosition(it))
                } }
            )
            nodesPositions.values.forEach { list ->
                list.asSequence()
                    .zipWithNext()
                    .filter { it.first != it.second }
                    .map { it.first.distanceTo(it.second) }
                    .toList() shouldBeSortedWith {
                        d1: Double, d2: Double -> when {
                            abs(d2 - d1) < EPSILON -> 0
                            d2 > d1 -> 1
                            else -> -1
                        }
                    }
            }
        }
    }

    "cohesion gives importance to the other members of the group during an evacuation" {
        loadYamlSimulation<T, P>("cohesion.yml").startSimulation(
            finished = { e, _, _ -> e.nodes.asSequence()
                    .filterIsInstance<Pedestrian<T>>()
                    .groupBy { it.membershipGroup }
                    .values
                    .forEach {
                        for (nodePos in it.map { node -> e.getPosition(node) }) {
                            for (otherPos in (it.map { node -> e.getPosition(node) }.minusElement(nodePos))) {
                                nodePos.distanceTo(otherPos) shouldBeLessThan 4.0
                            }
                        }
                    }
            },
            numSteps = 20000
        )
    }

    "nodes using separation behavior keep a distance to each other" {
        loadYamlSimulation<T, P>("separation.yml").startSimulation(
            finished = { e, _, _ -> with(e.nodes.map { e.getPosition(it) }) {
                for (nodePos in this) {
                    for (otherPos in (this.minusElement(nodePos))) {
                        nodePos.distanceTo(otherPos) shouldBeGreaterThan 6.0
                    }
                }
            } },
            numSteps = 30000
        )
    }

    "obstacle avoidance let nodes reach destinations behind obstacles" {
        loadYamlSimulation<T, P>("obstacle-avoidance.yml").startSimulation(
            finished = { e, _, _ -> e.nodes.forEach {
                e.getPosition(it).distanceTo(e.makePosition(600.0, 240.0)) shouldBeLessThan 10.0
            } },
            numSteps = 22000
        )
    }
})
