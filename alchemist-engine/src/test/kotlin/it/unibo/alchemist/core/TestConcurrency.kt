/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.terminators.StepCount
import it.unibo.alchemist.model.timedistributions.DiracComb
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * This class tests some basic Commands, like pause and start.
 */
internal class TestConcurrency {
    /**
     * Test if the status of a [Engine] changes as expected.
     */
    @Test
    fun `simulation commands should interleave`() {
        val incarnation = BiochemistryIncarnation()
        val environment = Continuous2DEnvironment(incarnation)
        val node = GenericNode(incarnation, environment)
        environment.linkingRule = NoLinks()
        val timeDistribution = DiracComb<Double>(1.0)
        val reaction = Event(node, timeDistribution)
        node.addReaction(reaction)
        environment.addNode(node, environment.makePosition(0, 0))
        val inWaitCount = 100
        val simulation: Simulation<*, *> = Engine(environment)
        environment.addTerminator(StepCount(10))
        simulation.pause()
        val container: ExecutorService = Executors.newFixedThreadPool(inWaitCount + 1)
        container.submit(simulation)
        Assertions.assertNotEquals(
            Status.RUNNING,
            simulation.waitFor(Status.RUNNING, 2, TimeUnit.SECONDS),
        )
        Assertions.assertEquals(Status.PAUSED, simulation.waitFor(Status.PAUSED, 1, TimeUnit.MINUTES))
        /*
         * Launch a hundred waiting processes, make sure they are started, then make sure everyone got notified
         */
        val latch = CountDownLatch(inWaitCount)
        val waitList: List<Future<Status?>> =
            generateSequence {
                container.submit<Status?>(
                    Callable {
                        latch.countDown()
                        simulation.waitFor(Status.RUNNING, 1, TimeUnit.MINUTES)
                    },
                )
            }.take(inWaitCount)
                .toList()
        Assertions.assertTrue(latch.await(1, TimeUnit.MINUTES))
        // All threads are started, wait a bit of additional time to make sure they reached wait status
        Thread.sleep(1000)
        simulation.play()
        for (result in waitList) {
            Assertions.assertEquals(Status.RUNNING, result.get())
        }
        /*
         * this test does only 10 steps, so, after reaching RUNNING status, the simulation stops almost
         * instantly, because it takes a very little time to perform 10 steps, since in every step the
         * simulation executes the fake reaction you can see below, which simply does nothing.
         */
        Assertions.assertEquals(
            Status.TERMINATED,
            simulation.waitFor(Status.TERMINATED, 1, TimeUnit.MINUTES),
        )
        /*
         * the method must return immediately with a message error because is not
         * possible to reach RUNNING or PAUSED status while in STOPPED
         */
        Assertions.assertEquals(
            Status.TERMINATED,
            simulation.waitFor(Status.RUNNING, 100, TimeUnit.MILLISECONDS),
        )
    }
}
