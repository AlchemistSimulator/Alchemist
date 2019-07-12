package it.unibo.alchemist.test

import io.kotlintest.matchers.collections.shouldBeSortedWith
import io.kotlintest.matchers.doubles.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import kotlin.math.abs

private const val EPSILON = 0.001

class TestSteeringBehaviors<T, P : Position2D<P>> : StringSpec({

    "seek" {
        loadYamlSimulation<T, P>("seek.yml").startSimulation(
            finished = { e, _, _ -> e.nodes.forEach {
                e.getPosition(it) shouldBe e.makePosition(0.0, 0.0)
            } }
        )
    }

    "flee" {
        loadYamlSimulation<T, P>("flee.yml").startSimulation(
            finished = { e, _, _ -> e.nodes.forEach {
                e.getPosition(it).getDistanceTo(e.makePosition(0, 0)) shouldBeGreaterThan 100.0
            } }
        )
    }

    "arrive" {
        with(loadYamlSimulation<T, P>("arrive.yml")) {
            val nodesPositions: Map<Node<T>, MutableList<P>> = nodes.map { it to mutableListOf<P>() }.toMap()
            startSimulation(
                stepDone = { e, _, _, _ -> e.nodes.forEach {
                    nodesPositions[it]?.add(e.getPosition(it))
                } }
            )
            nodesPositions.values.forEach { list ->
                list.zipWithNext()
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