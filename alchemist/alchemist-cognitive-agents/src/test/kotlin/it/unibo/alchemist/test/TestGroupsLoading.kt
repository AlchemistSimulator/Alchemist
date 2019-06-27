package it.unibo.alchemist.test

import io.kotlintest.TestCase
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.cognitiveagents.groups.Family
import it.unibo.alchemist.loader.displacements.Circle
import it.unibo.alchemist.loader.displacements.Displacement
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import org.apache.commons.math3.random.MersenneTwister
import kotlin.properties.Delegates

private const val NUM_FAMILIES = 3

private val ENVIRONMENT = Continuous2DEnvironment<Any>()
private val LINKING_RULE = NoLinks<Any, Euclidean2DPosition>()
private val RANDOM = MersenneTwister(1)
private var displacement: Displacement<Euclidean2DPosition> by Delegates.notNull()
private var nodes: Collection<Pedestrian<Any>> by Delegates.notNull()

class TestGroupsLoading : StringSpec({

    "families loading" {
        nodes = (0..NUM_FAMILIES).flatMap { Family(ENVIRONMENT, RANDOM, 2).members }
        displacement = Circle<Euclidean2DPosition>(ENVIRONMENT, RANDOM, nodes.size, 0.0, 0.0, 10.0)
        with(displacement.iterator()) {
            nodes.onEach { println(it.membershipGroup) }.forEach { ENVIRONMENT.addNode(it, this.next()) }
        }
        ENVIRONMENT.startSimulation()
    }
}) {
    override fun beforeTest(testCase: TestCase) {
        ENVIRONMENT.linkingRule = LINKING_RULE
    }
}
