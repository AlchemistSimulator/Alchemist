/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.layers.StepLayer;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 *
 */
public class TestBiomolLayer {

    private static final Incarnation<Double, Euclidean2DPosition> INCARNATION = new BiochemistryIncarnation<>();

    /**
     * Test if cell status is correctly updated in movement.
     */
    @Test
    public void testBiomolStepLayer() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        final Biomolecule b = new Biomolecule("B");
        final Layer<Double, Euclidean2DPosition> bLayer = new StepLayer<>(10000d, 0d);
        final CellNode<Euclidean2DPosition> cellNode = new CellNodeImpl<>(env);
        final MersenneTwister rand = new MersenneTwister(0);
        final Molecule a = new Biomolecule("A");
        final Reaction<Double> underTest = INCARNATION.createReaction(
            rand, env, cellNode,
            INCARNATION.createTimeDistribution(rand, env, cellNode, "1"),
            "[B in env] --> [A]"
        );
        cellNode.addReaction(underTest);
        cellNode.addReaction(INCARNATION.createReaction(
            rand, env, cellNode, new DiracComb<>(100d), "[] --> [BrownianMove(10)]"
        ));
        cellNode.setConcentration(a, 0d);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        env.addNode(cellNode, new Euclidean2DPosition(0, 0));
        env.addLayer(b, bLayer);
        final Simulation<Double, Euclidean2DPosition> sim = new Engine<>(env, 3000);
        sim.play();
        sim.addOutputMonitor(new OutputMonitor<Double, Euclidean2DPosition>() {
            private static final long serialVersionUID = -8801751097767369325L;
            @Override
            public void stepDone(final Environment<Double, Euclidean2DPosition> env, final Reaction<Double> r, final Time time, final long step) {
                final Euclidean2DPosition curPos = env.getPosition(env.getNodeByID(0));
                assertEquals(curPos.getX() > 0 && curPos.getY() > 0, underTest.canExecute());
            }
            @Override
            public void initialized(final Environment<Double, Euclidean2DPosition> env) {
                stepDone(env, null, null, 0);
            }
            @Override
            public void finished(final Environment<Double, Euclidean2DPosition> env, final Time time, final long step) { }
        });
        sim.run();
    }
}
