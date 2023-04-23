/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.tests;


import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.core.Status;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.linkingrules.NoLinks;
import it.unibo.alchemist.model.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.reactions.Event;
import it.unibo.alchemist.model.timedistributions.DiracComb;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests some basic Commands, like pause and start.
 */
class TestConcurrency {

    private Environment<Double, Euclidean2DPosition> environment;

    /**
     * Setup phase.
     */
    @BeforeEach
    public void setUp() {
        final Incarnation<Double, Euclidean2DPosition> incarnation = new BiochemistryIncarnation();
        environment = new Continuous2DEnvironment<>(incarnation);
        final var n = new GenericNode<>(incarnation, environment);
        environment.setLinkingRule(new NoLinks<>());
        final var td = new DiracComb<Double>(1);
        final var r = new Event<>(n, td);
        n.addReaction(r);
        environment.addNode(n, environment.makePosition(0, 0));
    }

    /**
     *
     * Test if the status of a {@link Engine} changes as expected.
     *
     * @throws InterruptedException fails
     * @throws ExecutionException fails
     */
    @Test
    @SuppressFBWarnings(
            value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "We don't need the status of the Runnable"
    )
    void testCommandInterleaving() throws InterruptedException, ExecutionException {
        final int inWaitCount = 100;
        final Simulation<?, ?> sim = new Engine<>(environment, 10);
        sim.pause();
        final ExecutorService container = Executors.newFixedThreadPool(inWaitCount + 1);
        container.submit(sim);
        assertNotEquals(Status.RUNNING, sim.waitFor(Status.RUNNING, 2, TimeUnit.SECONDS));
        assertEquals(Status.PAUSED, sim.waitFor(Status.PAUSED, 1, TimeUnit.MINUTES));
        /*
         * Launch a hundred waiting processes, make sure they are started, then make sure everyone got notified
         */
        final CountDownLatch latch = new CountDownLatch(inWaitCount);
        final ImmutableList<Future<Status>> waitList = IntStream.range(0, inWaitCount)
            .mapToObj(it -> container.submit(() -> {
                latch.countDown();
                return sim.waitFor(Status.RUNNING, 1, TimeUnit.MINUTES);
            }))
            .collect(ImmutableList.toImmutableList());
        assertTrue(latch.await(1, TimeUnit.MINUTES));
        // All threads are started, wait a bit of additional time to make sure they reached wait status
        Thread.sleep(1000);
        sim.play();
        for (final Future<Status> result: waitList) {
            assertEquals(Status.RUNNING, result.get());
        }
        /*
         * this test does only 10 steps, so, after reaching RUNNING status, the simulation stops almost
         * instantly, because it takes a very little time to perform 10 steps, since in every step the
         * simulation executes the fake reaction you can see below, which simply does nothing.
         */
        assertEquals(Status.TERMINATED, sim.waitFor(Status.TERMINATED, 1, TimeUnit.MINUTES));
        /*
         * the method must return immediately with a message error because is not
         * possible to reach RUNNING or PAUSED status while in STOPPED
         */
        assertEquals(Status.TERMINATED, sim.waitFor(Status.RUNNING, 100, TimeUnit.MILLISECONDS));
    }

}
