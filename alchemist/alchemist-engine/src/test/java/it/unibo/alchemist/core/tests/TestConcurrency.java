/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.tests;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.AbstractNode;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class tests some basic Commands, like pause and start.
 */
public class TestConcurrency {

    private Environment<Object, Euclidean2DPosition> env;

    /**
     * Setup phase.
     */
    @BeforeEach
    public void setUp() {
        env = new Continuous2DEnvironment<>();
        final Node<Object> n = new DummyNode(env);
        env.setLinkingRule(new NoLinks<>());
        final TimeDistribution<Object> td = new DiracComb<>(1);
        final Reaction<Object> r = new Event<>(n, td);
        n.addReaction(r);
        env.addNode(n, env.makePosition(0, 0));
    }

    /**
     *
     * Test if the status of a {@link Engine} changes as expected.
     *
     * @throws InterruptedException fails
     * @throws ExecutionException fails
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We don't need the status of the Runnable")
    public void testCommandInterleaving() throws InterruptedException, ExecutionException {
        final Simulation<?, ?> sim = new Engine<>(env, 10);
        sim.pause();
        ForkJoinPool.commonPool().submit(sim);
        assertNotEquals(Status.RUNNING, sim.waitFor(Status.RUNNING, 2, TimeUnit.SECONDS));
        assertEquals(Status.PAUSED, sim.waitFor(Status.PAUSED, 1, TimeUnit.SECONDS));
        /*
         * Wait for running status then send a play command
         */
        final Future<Status> running = ForkJoinPool.commonPool().submit(() -> sim.waitFor(Status.RUNNING, 2, TimeUnit.SECONDS));
        Thread.sleep(100);
        sim.play();
        assertEquals(Status.RUNNING, running.get());
        /*
         * this test does only 10 steps, so, after reaching RUNNING status, the simulation stops almost
         * instantly, because it takes a very little time to perform 10 steps, since in every step the
         * simulation executes the fake reaction you can see below, which simply does nothing.
         */
        assertEquals(Status.TERMINATED, sim.waitFor(Status.TERMINATED, 1, TimeUnit.SECONDS));
        /*
         * the method must return immediately with a message error because is not
         * possible to reach RUNNING or PAUSED status while in STOPPED
         */
        assertThrows(RuntimeException.class, () -> sim.waitFor(Status.RUNNING, 100, TimeUnit.MILLISECONDS));
    }

    /**
     * Tests if the simulation ends correctly when it reaches the last step.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We don't need the status of the Runnable")
    public void newTest2() {
        final Simulation<?, ?> sim = new Engine<>(env, 10);
        final ExecutorService ex = Executors.newCachedThreadPool();
        ex.submit(sim);
        ex.submit(sim::play);
        sim.waitFor(Status.TERMINATED, 10, TimeUnit.SECONDS);
        verifyStatus(ex, sim, Status.TERMINATED);
    }

    private void verifyStatus(final ExecutorService ex, final Simulation<?, ?> sim, final Status s) {
        try {
            if (ex.isShutdown()) {
                if (!ex.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    fail("The thread did not end on time; its status is " + sim.getStatus());
                }
                assertTrue(ex.isTerminated());
            } else {
                Thread.sleep(100);
                assertEquals(s, sim.getStatus());
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private static final class DummyNode extends AbstractNode<Object> {
        private static final long serialVersionUID = 1L;
        private DummyNode(final Environment<?, ?> env) {
            super(env);
        }
        @Override
        protected Object createT() {
            return "";
        }
    }

}
