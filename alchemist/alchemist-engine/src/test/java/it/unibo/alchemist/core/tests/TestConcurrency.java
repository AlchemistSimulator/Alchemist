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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class tests some basic Commands, like pause and start.
 */
public class TestConcurrency {

    private static final Logger L = LoggerFactory.getLogger(Engine.class);

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
     * Test if the status of a {@link Engine} changes as expected.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We don't need the status of the Runnable")
    public void testCommandInterleaving() {
        final Simulation<?, ?> sim = new Engine<>(env, 10);
        final ExecutorService ex = Executors.newCachedThreadPool();
        ex.submit(sim);
        ex.submit(sim::pause);
        if (sim.waitFor(Status.RUNNING, 1, TimeUnit.SECONDS) != Status.RUNNING) { // after a second the method must return
            L.info("The status I was waiting for did not arrived! (as predicted)");
        } else {
            fail();
        }
        verifyStatus(ex, sim, Status.PAUSED);
        sim.waitFor(Status.PAUSED, 10, TimeUnit.MILLISECONDS);
        verifyStatus(ex, sim, Status.PAUSED);
        ex.submit(sim::play);
        sim.waitFor(Status.RUNNING, 1, TimeUnit.SECONDS); // the method must return instantly
        /*
         * this test does only 10 steps, so, after reaching RUNNING status, the simulation stops almost
         * instantly, because it takes a very little time to perform 10 steps, since in every step the
         * simulation executes the fake reaction you can see below, which simply does nothing.
         */
        verifyStatus(ex, sim, Status.TERMINATED);
        /*
         * the method must return immediately with a message error because is not
         * possible to reach RUNNING or PAUSED status while in STOPPED
         */
        sim.waitFor(Status.RUNNING, 10, TimeUnit.MILLISECONDS);
        ex.shutdown();
        verifyStatus(ex, sim, Status.TERMINATED);
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
        sim.waitFor(Status.TERMINATED, 1, TimeUnit.SECONDS);
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
