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
import it.unibo.alchemist.model.implementations.layers.BiomolGradientLayer;
import it.unibo.alchemist.model.implementations.layers.BiomolStepLayer;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.interfaces.BiomolLayer;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 *
 */
public class TestBiomolLayer {


    private static final Incarnation<Double> INCARNATION = new BiochemistryIncarnation();
    /**
     * Test if cell status is correctly updated in movement.
     */
    @Test
    public void testBiomolStepLayer() {
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
                rand, env, cellNode, new DiracComb<Double>(1000d), "[] --> [BrownianMove(10)]"
                ));
        cellNode.setConcentration(a, 0d);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.EuclideanDistance<>(2));
        env.addNode(cellNode, new Continuous2DEuclidean(0, 0));
        env.addLayer(bLayer);

        final Simulation<Double> sim = new Engine<>(env, 3000);
        sim.addCommand(new Engine.StateCommand<Double>().run().build());
        sim.addOutputMonitor(new OutputMonitor<Double>() {
            /**
             * 
             */
            private static final long serialVersionUID = -8801751097767369325L;

            @Override
            public void stepDone(final Environment<Double> env, final Reaction<Double> r, final Time time, final long step) {
                final Position curPos = env.getPosition(env.getNodes().stream().findAny().get());
                System.out.println(curPos);
                if (curPos.getCoordinate(0) >= 0 && curPos.getCoordinate(1) >= 0) {
                    schedulability(true);
                } else {
                    schedulability(false);
                }
            }

            @Override
            public void initialized(final Environment<Double> env) {
                schedulability(false);
            }

            @Override
            public void finished(final Environment<Double> env, final Time time, final long step) {
            }

            private void schedulability(final boolean val) {
                final double time = underTest.getTau().toDouble();
                if (val) {
                    assertTrue("The reaction should be schedulable at time " + time,  time >= 0 && !Double.isNaN(time) && !Double.isInfinite(time));
                } else {
                    assertEquals("The reaction should not be schedulable at time " + time, Double.POSITIVE_INFINITY, time, 0.00000001d);
                }
            }
        });
        sim.run();
    }

    /**
     * 
     */
    @Test
    public void testBiomolGradientLayer() {
        final Environment<Double> env = new BioRect2DEnvironment();
        final Position direction = new Continuous2DEuclidean(0, 1);
        final Biomolecule b = new Biomolecule("B");
        final BiomolLayer bgLayer = new BiomolGradientLayer(direction, 1, 0, b);
        final CellNode cellNode = new CellNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister(0);
        final Reaction<Double> underTest = INCARNATION.createReaction(
                rand, env, cellNode,
                INCARNATION.createTimeDistribution(rand, env, cellNode, "1"),
                "[] --> [ChemiotaxisMove(1, true)]"
                );
        cellNode.addReaction(underTest);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.EuclideanDistance<>(2));
        env.addNode(cellNode, new Continuous2DEuclidean(0, 0));
        env.addLayer(bgLayer);

        final Simulation<Double> sim = new Engine<>(env, 1000);
        sim.addCommand(new Engine.StateCommand<Double>().run().build());
        sim.addOutputMonitor(new OutputMonitor<Double>() {

            /**
             * 
             */
            private static final long serialVersionUID = 5946592128892608552L;
            private int i = 0;

            @Override
            public void stepDone(final Environment<Double> env, final Reaction<Double> r, final Time time, final long step) {
                assertEquals(new Continuous2DEuclidean(0, i), env.getPosition(cellNode));
                i++;
            }

            @Override
            public void initialized(final Environment<Double> env) {
                assertEquals(new Continuous2DEuclidean(0, 0), env.getPosition(env.getNodes().stream().findAny().get()));
            }

            @Override
            public void finished(final Environment<Double> env, final Time time, final long step) {
                assertEquals(new Continuous2DEuclidean(0, 1000), env.getPosition(env.getNodes().stream().findAny().get()));
            }
        });
        sim.run();
    }
}
