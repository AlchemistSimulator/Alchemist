package it.unibo.alchemist.test

import io.kotlintest.matchers.collections.shouldBeSortedWith
import io.kotlintest.matchers.doubles.shouldBeGreaterThan
import io.kotlintest.matchers.doubles.shouldBeLessThan
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.implementations.actions.utils.origin
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import kotlin.math.abs

private const val EPSILON = 0.001

class TestSteeringBehaviors<T, P : Position2D<P>> : StringSpec({

    "nodes seeking a target must approach it" {
        val startDistances = mutableMapOf<Node<T>, Double>()
        val endDistances = mutableMapOf<Node<T>, Double>()
        loadYamlSimulation<T, P>("seek.yml").startSimulation(
            initialized = { e -> e.nodes.forEach {
                startDistances[it] = e.getPosition(it).getDistanceTo(e.origin())
            } },
            finished = { e, _, _ -> e.nodes.forEach {
                endDistances[it] = e.getPosition(it).getDistanceTo(e.origin())
            } }
        ).nodes.forEach { startDistances[it]!! shouldBeGreaterThan endDistances[it]!! }
    }

    "nodes fleeing from a target must go away from it" {
        val startDistances = mutableMapOf<Node<T>, Double>()
        val endDistances = mutableMapOf<Node<T>, Double>()
        loadYamlSimulation<T, P>("flee.yml").startSimulation(
            initialized = { e -> e.nodes.forEach {
                startDistances[it] = e.getPosition(it).getDistanceTo(e.origin())
            } },
            finished = { e, _, _ -> e.nodes.forEach {
                endDistances[it] = e.getPosition(it).getDistanceTo(e.origin())
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
                    .map { it.first.getDistanceTo(it.second) }
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
})