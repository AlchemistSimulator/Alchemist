package it.unibo.alchemist.agents.test

import io.kotlintest.TestCase
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.agents.cognitive.CognitivePedestrian2D
import it.unibo.alchemist.agents.cognitive.CognitivePedestrian
import it.unibo.alchemist.behaviours.CognitiveBehaviour
import it.unibo.alchemist.influences.DangerousInfluence
import it.unibo.alchemist.loader.displacements.Circle
import it.unibo.alchemist.loader.displacements.Displacement
import it.unibo.alchemist.model.implementations.actions.BrownianMove
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb
import it.unibo.alchemist.model.interfaces.Action
import org.apache.commons.math3.random.MersenneTwister
import kotlin.properties.Delegates

typealias T = Double

private const val NUM_NODES = 50

private val GENDERS = listOf("male", "female")

private val AGES = listOf("child", "adult", "elderly")

private val ENVIRONMENT = Continuous2DEnvironment<T>()
private val LINKING_RULE = NoLinks<T, Euclidean2DPosition>()
private val TIME_DISTRIBUTION = DiracComb<T>(1.0)
private val DANGER_MOLECULE = SimpleMolecule("DANGER")
private val DANGER_LAYER = DangerousInfluence<Euclidean2DPosition>(0.0, 0.0, 2.0)
private val RANDOM = MersenneTwister(3)
private val MOVEMENT_STRATEGY: (CognitivePedestrian<T>) -> Action<T> = {
    BrownianMove(ENVIRONMENT, it, RANDOM, it.walkingSpeed * TIME_DISTRIBUTION.rate)
}
private var displacement: Displacement<Euclidean2DPosition> by Delegates.notNull()
private var nodes: Collection<CognitivePedestrian<T>> by Delegates.notNull()

class TestFeelsTransmission : StringSpec({

    "danger layer affects cognitive pedestrians" {
        // You can show the difference running the simulation with or without the following line
        ENVIRONMENT.addLayer(DANGER_MOLECULE, DANGER_LAYER)
        ENVIRONMENT.startSimulationWithoutParameters()
        nodes.forEach { println("${it.id} -> ${it.dangerBelief()}") }
    }
}) {
    override fun beforeTest(testCase: TestCase) {
        ENVIRONMENT.linkingRule = LINKING_RULE
        nodes = (0..NUM_NODES).map { CognitivePedestrian2D(ENVIRONMENT, RANDOM, AGES[it % AGES.size], GENDERS[it % GENDERS.size]) }
        displacement = Circle<Euclidean2DPosition>(ENVIRONMENT, RANDOM, nodes.size, 0.0, 0.0, 10.0)
        with(displacement.iterator()) {
            nodes.forEach {
                with(CognitiveBehaviour(it, TIME_DISTRIBUTION)) {
                    actions = listOf(MOVEMENT_STRATEGY(it))
                    it.addReaction(this)
                }
                ENVIRONMENT.addNode(it, this.next())
            }
        }
    }
}