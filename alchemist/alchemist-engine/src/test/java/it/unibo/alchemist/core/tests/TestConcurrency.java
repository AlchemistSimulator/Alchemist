package it.unibo.alchemist.core.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * This class tests some basic Commands, like pause and start.
 */
public class TestConcurrency {

    private static final Logger L = LoggerFactory.getLogger(Engine.class);

    private Environment<Object> env;

    /**
     * Setup phase.
     */
    @Before
    public void setUp() {
        env = new Continuous2DEnvironment<>();
        final Node<Object> n = new GenericNode<Object>(env) {
            private static final long serialVersionUID = 1L;
            @Override
            protected Object createT() {
                return "";
            }
        };
        env.setLinkingRule(new NoLinks<>());
        final TimeDistribution<Object> td = new DiracComb<>(1);
        final Reaction<Object> r = new Event<>(n, td);
        n.addReaction(r);
        env.addNode(n, env.makePosition(0, 0));
    }

    /**
     * Test if the status of a {@link Engine} changes accordingly to the methods
     * provided by {@link Engine.StateCommand}.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We don't need the status of the Runnable")
    public void newNewTest1() {

        final Simulation<Object> sim = new Engine<>(env, 10);
        final ExecutorService ex = Executors.newCachedThreadPool();

        ex.submit(sim);

        ex.submit(() -> sim.pause());
        if (sim.waitFor(Status.RUNNING, 1, TimeUnit.SECONDS) != Status.RUNNING) { // after a second the method must return
            L.info("The status I was waiting for did not arrived! (as predicted)");
        } else {
            fail();
        }
        verifyStatus(ex, sim, Status.PAUSED, 1000, 100);

        sim.waitFor(Status.PAUSED, 0, TimeUnit.DAYS);
        verifyStatus(ex, sim, Status.PAUSED, 1000, 100);

        ex.submit(() -> sim.play());
        sim.waitFor(Status.RUNNING, 1, TimeUnit.SECONDS); // the method must return instantly

        /*
         * this test does only 10 steps, so, after reaching RUNNING status, the simulation stops almost
         * instantly, because it takes a very little time to perform 10 steps, since in every step the 
         * simulation executes the fake reaction you can see below, which simply does nothing.
         */
        verifyStatus(ex, sim, Status.TERMINATED, 1000, 100);

        /*
         * the method must return immediatly with a message error because is not
         * possible to reach RUNNING or PAUSED status while in STOPPED
         */
        sim.waitFor(Status.RUNNING, 0, TimeUnit.DAYS);

        ex.shutdown();
        verifyStatus(ex, sim, Status.TERMINATED, 1000, 100);
    }

    /**
     * Tests if the simulation ends correctly when it reaches the last step.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We don't need the status of the Runnable")
    public void newTest2() {
        final Simulation<Object> sim = new Engine<>(env, 10);
        final ExecutorService ex = Executors.newCachedThreadPool();
        ex.submit(sim);
        ex.submit(() -> sim.play());
        sim.waitFor(Status.TERMINATED, 1, TimeUnit.SECONDS);
        verifyStatus(ex, sim, Status.TERMINATED, 1000, 100);
    }

    private void verifyStatus(final ExecutorService ex, final Simulation<?> sim, final Status s,
            final int terminationTimeout, final int sleepTimeout) {
        try {
            if (ex.isShutdown()) {
                if (!ex.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS)) {
                    fail("The thread did not end on time; its status is " + sim.getStatus());
                }
                assertTrue(ex.isTerminated());
            } else {
                Thread.sleep(sleepTimeout);
                assertEquals(s, sim.getStatus());
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

}
