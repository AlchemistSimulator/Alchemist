package it.unibo.alchemist.core.tests;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//CHECKSTYLE:OFF
import static org.junit.Assert.*;
//CHECKSTYLE:ON
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * This class tests some basic Commands, like pause and start.
 */
public class TestCommands {

    private static final Logger L = LoggerFactory.getLogger(Engine.class);

    /**
     * Test if the status of a {@link Engine} changes accordingly to the methods
     * provided by {@link Engine.StateCommand}.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We don't need the status of the Runnable")
    public void newNewTest1() {

        final Simulation<Object> sim = new Engine<>(new DummyEnvironment(), 10);
        final ExecutorService ex = Executors.newCachedThreadPool();

        ex.submit(sim);

        ex.submit(() -> sim.addCommand(new Engine.StateCommand<>().pause().build()));
        if (sim.waitFor(Status.RUNNING, 1, TimeUnit.SECONDS) != Status.RUNNING) { // after a second the method must return
            L.info("The status I was waiting for did not arrived! (as predicted)");
        } else {
            fail();
        }
        verifyStatus(ex, sim, Status.PAUSED, 1000, 100);

        ex.submit(() -> sim.addCommand((s) -> {
            L.info("I am a " + s.getClass() + " and my status is " + s.getStatus());
        }));
        sim.waitFor(Status.PAUSED, 0, TimeUnit.DAYS);
        verifyStatus(ex, sim, Status.PAUSED, 1000, 100);

        ex.submit(() -> sim.addCommand(new Engine.StateCommand<>().run().build()));
        sim.waitFor(Status.RUNNING, 1, TimeUnit.SECONDS); // the method must return instantly

        /*
         * this test does only 10 steps, so, after reaching RUNNING status, the simulation stops almost
         * instantly, because it takes a very little time to perform 10 steps, since in every step the 
         * simulation executes the fake reaction you can see below, which simply does nothing.
         */
        verifyStatus(ex, sim, Status.STOPPED, 1000, 100);

        /*
         * the method must return immediatly with a message error because is not
         * possible to reach RUNNING or PAUSED status while in STOPPED
         */
        sim.waitFor(Status.RUNNING, 0, TimeUnit.DAYS);

        ex.shutdown();
        verifyStatus(ex, sim, Status.STOPPED, 1000, 100);
    }

    /**
     * Tests if the simulation ends correctly when it reaches the last step.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We don't need the status of the Runnable")
    public void newTest2() {
        final Environment<Object> env = new DummyEnvironment();
        final Simulation<Object> sim = new Engine<>(env, 10);
        final ExecutorService ex = Executors.newCachedThreadPool();
        ex.submit(sim);
        ex.submit(() -> sim.addCommand(new Engine.StateCommand<>().run().build()));
        sim.waitFor(Status.STOPPED, 1, TimeUnit.SECONDS);
        verifyStatus(ex, sim, Status.STOPPED, 1000, 100);
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

    @SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS", justification = "We don't need equals nor compareTo")
    private static class DummyEnvironment implements Environment<Object> {
        private static final long serialVersionUID = 4097966732041667486L;
        private final Node<Object> node = new Node<Object>() {
            private static final long serialVersionUID = 1L;
            private final Reaction<Object> reaction = new Reaction<Object>() {
                private static final long serialVersionUID = 1L;
                private Time tau = new DoubleTime();
                @Override
                public int compareTo(final Reaction<Object> o) {
                    return 0;
                }
                @Override
                public boolean canExecute() {
                    return true;
                }
                @Override
                public Reaction<Object> cloneOnNewNode(final Node<Object> n) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void execute() {
                }
                @Override
                public List<? extends Action<Object>> getActions() {
                    return Collections.emptyList();
                }
                @Override
                public List<? extends Condition<Object>> getConditions() {
                    return Collections.emptyList();
                }

                @Override
                public List<? extends Molecule> getInfluencedMolecules() {
                    return Collections.emptyList();
                }

                @Override
                public List<? extends Molecule> getInfluencingMolecules() {
                    return Collections.emptyList();
                }

                @Override
                public Context getInputContext() {
                    return Context.LOCAL;
                }

                @Override
                public Node<Object> getNode() {
                    return node;
                }

                @Override
                public Context getOutputContext() {
                    return Context.LOCAL;
                }

                @Override
                public double getRate() {
                    return 1;
                }
                @Override
                public Time getTau() {
                    return tau;
                }

                @Override
                public TimeDistribution<Object> getTimeDistribution() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setActions(final List<? extends Action<Object>> a) {
                }

                @Override
                public void setConditions(final List<? extends Condition<Object>> c) {
                }

                @Override
                public void update(final Time curTime, final boolean executed, final Environment<Object> env) {
                    tau = tau.sum(new DoubleTime(1));
                } };
            @Override
            public Iterator<Reaction<Object>> iterator() {
                return getReactions().iterator();
            }
            @Override
            public void addReaction(final Reaction<Object> r) {
            }
            @Override
            public boolean contains(final Molecule mol) {
                return false;
            }
            @Override
            public int getChemicalSpecies() {
                return 0;
            }
            @Override
            public Object getConcentration(final Molecule mol) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<Molecule, Object> getContents() {
                return Collections.emptyMap();
            }

            @Override
            public int getId() {
                return 0;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Reaction<Object>> getReactions() {
                return Lists.<Reaction<Object>>newArrayList(reaction);
            }

            @Override
            public void removeConcentration(final Molecule mol) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void removeReaction(final Reaction<Object> r) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setConcentration(final Molecule mol, final Object c) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Node<Object> cloneNode() {
                throw new UnsupportedOperationException();
            }
            @Override
            public int compareTo(final Node<Object> o) {
                return 0;
            } };
        @Override
        public Iterator<Node<Object>> iterator() {
            return getNodes().iterator();
        }

        @Override
        public void addNode(final Node<Object> node, final Position p) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getDimensions() {
            return 0;
        }

        @Override
        public double getDistanceBetweenNodes(final Node<Object> n1, final Node<Object> n2) {
            return 0;
        }

        @Override
        public Neighborhood<Object> getNeighborhood(final Node<Object> center) {
            return new Neighborhood<Object>() {
                private static final long serialVersionUID = 1L;
                @Override
                public Iterator<Node<Object>> iterator() {
                    return Collections.emptyIterator();
                }
                @Override
                public void addNeighbor(final Node<Object> neigh) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Neighborhood<Object> clone() throws CloneNotSupportedException {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean contains(final Node<Object> n) {
                    return false;
                }
                @Override
                public boolean contains(final int n) {
                    return false;
                }
                @Override
                public Set<? extends Node<Object>> getBetweenRange(final double min, final double max) {
                    return Collections.emptySet();
                }

                @Override
                public Node<Object> getCenter() {
                    return node;
                }
                @Override
                public Node<Object> getNeighborById(final int id) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Node<Object> getNeighborByNumber(final int num) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Collection<? extends Node<Object>> getNeighbors() {
                    return Collections.emptyList();
                }
                @Override
                public boolean isEmpty() {
                    return true;
                }
                @Override
                public void removeNeighbor(final Node<Object> neighbor) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public int size() {
                    return 0;
                }
            };
        }

        @Override
        public Node<Object> getNodeByID(final int id) {
            return node;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<Node<Object>> getNodes() {
            return Lists.<Node<Object>>newArrayList(node);
        }

        @Override
        public int getNodesNumber() {
            return 1;
        }

        @Override
        public Collection<Node<Object>> getNodesWithinRange(final Node<Object> center, final double range) {
            return Collections.emptyList();
        }

        @Override
        public Collection<Node<Object>> getNodesWithinRange(final Position center, final double range) {
            return Collections.emptyList();
        }

        @Override
        public double[] getOffset() {
            return new double[] {};
        }

        @Override
        public Position getPosition(final Node<Object> node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPreferredMonitor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double[] getSize() {
            return new double[] {};
        }

        @Override
        public void moveNode(final Node<Object> node, final Position direction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void moveNodeToPosition(final Node<Object> node, final Position position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeNode(final Node<Object> node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLinkingRule(final LinkingRule<Object> rule) {
        }

        @Override
        public LinkingRule<Object> getLinkingRule() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addLayer(final Layer<Object> l) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<Layer<Object>> getLayers() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}