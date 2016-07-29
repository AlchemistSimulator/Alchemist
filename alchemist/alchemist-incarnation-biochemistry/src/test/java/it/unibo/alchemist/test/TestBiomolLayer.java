package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.layers.BiomolStepLayer;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.BiomolLayer;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

public class TestBiomolLayer {

    
    private static final Incarnation<Double> INCARNATION = new BiochemistryIncarnation();
    /**
     * Test if cell status is correctly updated in movement.
     */
    @Test
    public void test1() {
        final Environment<Double> env = new BioRect2DEnvironment();
        final Biomolecule b = new Biomolecule("B");
        final BiomolLayer bLayer = new BiomolStepLayer(100d, 0d, b);
        final CellNode cellNode = new CellNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister(0);
        final Molecule a = new Biomolecule("A");
        final Reaction<Double> underTest = INCARNATION.createReaction(
                rand, env, cellNode,
                INCARNATION.createTimeDistribution(rand, env, cellNode, "1"),
                "[B in env] --> [A]"
                );
        cellNode.addReaction(underTest);
        cellNode.addReaction(new BiochemistryIncarnation().createReaction(
                rand, env, cellNode, new DiracComb<Double>(0.001d), "[] --> [MoveForwardAndTeleport(50, -20, 20)]"
                ));
        cellNode.setConcentration(a, 0d);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.EuclideanDistance<>(2));
        env.addNode(cellNode, new Continuous2DEuclidean(-20, 20));
        env.addLayer(bLayer);

        final Simulation<Double> sim = new Engine<>(env, 3000);
        sim.addCommand(new Engine.StateCommand<Double>().run().build());
        sim.run();
        sim.addOutputMonitor(new OutputMonitor<Double>() {
            @Override
            public void stepDone(Environment<Double> env, Reaction<Double> r, Time time, long step) {
                final double curTime = time.toDouble();
                if (curTime < 1000d) {
                    schedulability(false);
                } else if (curTime < 2000d) {
                    schedulability(true);
                } else {
                    schedulability(false);
                }
            }
            
            @Override
            public void initialized(Environment<Double> env) {
                schedulability(false);
            }
            
            @Override
            public void finished(Environment<Double> env, Time time, long step) {
                schedulability(false);
            }
            private void schedulability(final boolean val) {
                final double time = underTest.getTau().toDouble();
                if (val) {
                    assertTrue("The reaction should be schedulable at time " + time,  time > 0 && !Double.isNaN(time) && !Double.isInfinite(time));
                } else {
                    assertEquals("The reaction should not be schedulable at time " + time, DoubleTime.INFINITE_TIME, underTest.getTau());
                }
            }
        });
    }
}
