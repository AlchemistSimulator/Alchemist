package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode
import it.unibo.alchemist.model.implementations.reactions.Event
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.math3.random.MersenneTwister
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.protelis.lang.datatype.DatatypeFactory
import java.util.Optional

class TestGetPosition {
    private val env: Environment<Any> = Continuous2DEnvironment()
    private val node = ProtelisNode(env)
    private val rng = MersenneTwister(0)
    private val reaction = Event(node, ExponentialTime(1.0, rng))
    private val action = RunProtelisProgram(env, node, reaction, rng, "self.getCoordinates()")

    @Before
    fun setUp() {
        env.linkingRule = NoLinks()
        reaction.actions = listOf(action)
        node.addReaction(reaction)
        env.addNode(node, env.makePosition(1, 1))
    }

    @Test
    fun testGetPosition() {
        val sim: Simulation<Any> = Engine(env, 100)
        sim.addOutputMonitor(object : OutputMonitor<Any> {
            override fun finished(environment: Environment<Any>?, time: Time?, step: Long) { }
            override fun initialized(environment: Environment<Any>?) { }
            override fun stepDone(env: Environment<Any>?, r: Reaction<Any>?, time: Time?, step: Long) =
                    Assert.assertEquals(
                            DatatypeFactory.createTuple(1.0, 1.0),
                            node.getConcentration(action.asMolecule())
                    )
        })
        sim.play()
        sim.run()
        Assert.assertEquals(Optional.empty<Any>(), sim.error)
    }
}