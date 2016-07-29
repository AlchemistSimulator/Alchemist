package it.unibo.alchemist.test;

import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.layers.BiomolStepLayer;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.interfaces.BiomolLayer;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;

public class TestBiomolLayer {

    /**
     * Test if cell status is correctly updated in movement.
     */
    @Test
    public void test1() {
        final Environment<Double> env = new BioRect2DEnvironment();
        final Biomolecule b = new Biomolecule("B");
        final BiomolLayer bLayer = new BiomolStepLayer(0d, 100d, b);
        final CellNode cellNode = new CellNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        cellNode.addReaction(new BiochemistryIncarnation().createReaction(
                rand, env, cellNode, new DiracComb<Double>(1d), "[B in env] --> [A]"
                ));
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
        assertTrue("conA = " + cellNode.getConcentration(a), cellNode.getConcentration(a) == 1000);
    }
}
