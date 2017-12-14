package it.unibo.alchemist.test

import org.junit.Test
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode
import it.unibo.alchemist.model.implementations.reactions.Event
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.MersenneTwister
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.Reaction
import org.junit.Assert
import org.protelis.lang.datatype.DatatypeFactory
import org.junit.Before
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import java.util.Optional

class TestGetPosition {

    val Environment<Object> env = new Continuous2DEnvironment()
    val ProtelisNode node = new ProtelisNode(env)
    val RandomGenerator rng = new MersenneTwister(0)
    val Event<Object> reaction = new Event(node, new ExponentialTime(1, rng))
    val RunProtelisProgram action = new RunProtelisProgram(env, node, reaction, rng, "self.getCoordinates()")

    @Before
    def setUp() {
        env.setLinkingRule(new NoLinks())
        reaction.setActions(#[action])
        node.addReaction(reaction)
        env.addNode(node, env.makePosition(1, 1))
    }

    @Test
    def testGetPosition() {
        val Simulation<Object> sim = new Engine(env, 100)
        sim.addOutputMonitor(new OutputMonitor<Object>(){
            override finished(Environment<Object> env, Time time, long step) { }
            override initialized(Environment<Object> env) { }
            override stepDone(Environment<Object> env, Reaction<Object> r, Time time, long step) {
                Assert.assertEquals(DatatypeFactory.createTuple(1.0, 1.0), node.getConcentration(action.asMolecule))
            }
        })
        sim.play
        sim.run
        Assert.assertEquals(Optional.empty, sim.error)
    }

}