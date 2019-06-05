/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.reactions.Event
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.math3.random.MersenneTwister
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.protelis.lang.datatype.DatatypeFactory
import java.util.Optional

class TestGetPosition {
    private val env: Environment<Any, Euclidean2DPosition> = Continuous2DEnvironment()
    private val node = ProtelisNode(env)
    private val rng = MersenneTwister(0)
    private val reaction = Event(node, ExponentialTime(1.0, rng))
    private val action = RunProtelisProgram(env, node, reaction, rng, "self.getCoordinates()")

    @BeforeEach
    fun setUp() {
        env.linkingRule = NoLinks()
        reaction.actions = listOf(action)
        node.addReaction(reaction)
        env.addNode(node, env.makePosition(1, 1))
    }

    @Test
    fun testGetPosition() {
        val sim: Simulation<Any, Euclidean2DPosition> = Engine(env, 100)
        sim.addOutputMonitor(object : OutputMonitor<Any, Euclidean2DPosition> {
            override fun finished(environment: Environment<Any, Euclidean2DPosition>?, time: Time?, step: Long) { }
            override fun initialized(environment: Environment<Any, Euclidean2DPosition>?) { }
            override fun stepDone(env: Environment<Any, Euclidean2DPosition>?, r: Reaction<Any>?, time: Time?, step: Long) {
                if (step > 0) {
                    Assertions.assertEquals(
                        DatatypeFactory.createTuple(1.0, 1.0),
                        node.getConcentration(action.asMolecule())
                    )
                }
            }
        })
        sim.play()
        sim.run()
        Assertions.assertEquals(Optional.empty<Any>(), sim.error)
    }
}