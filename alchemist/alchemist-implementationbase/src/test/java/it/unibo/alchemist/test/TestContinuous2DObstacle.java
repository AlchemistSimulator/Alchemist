/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import it.unibo.alchemist.model.implementations.environments.Continuous2DObstacles;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.utils.RectObstacle2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;

import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

/**
 */
public class TestContinuous2DObstacle {

    private static final RectObstacle2D R1021 = new RectObstacle2D(1, 0, 1, 1);
    private static final RectObstacle2D R0527 = new RectObstacle2D(0, 5, 2, -2);

    /**
     * 
     */
    @Test
    public void test() {
        final Continuous2DObstacles<Integer> env = new Continuous2DObstacles<>();
        env.setLinkingRule(new NoLinks<Integer>());
        env.addObstacle(R1021);
        env.addObstacle(R0527);

        assertEquals(new Continuous2DEuclidean(FastMath.nextAfter(1.0, 0.0), FastMath.nextAfter(1.0, 0.0)), env.next(0, 0, 1, 1));
        assertEquals(new Continuous2DEuclidean(0, 1), env.next(1, 1, 0, 0));
        assertEquals(new Continuous2DEuclidean(FastMath.nextAfter(1.0, 0.0), FastMath.nextAfter(0.5, 0.0)), env.next(0, 0, 2, 1));

        env.addNode(new DummyNode(env), new Continuous2DEuclidean(0, 0));
        assertEquals(env.getNodesNumber(), 1);
        env.addNode(new DummyNode(env), new Continuous2DEuclidean(1, 1));
        assertEquals(env.getNodesNumber(), 1);
        // CHECKSTYLE: MagicNumber OFF
        env.addNode(new DummyNode(env), new Continuous2DEuclidean(1.5, 0.5));
        assertEquals(env.getNodesNumber(), 1);
        env.addNode(new DummyNode(env), new Continuous2DEuclidean(1, 5));
        assertEquals(env.getNodesNumber(), 1);
        env.addNode(new DummyNode(env), new Continuous2DEuclidean(1, 2.999));
        assertEquals(env.getNodesNumber(), 2);
        assertEquals(env.getObstaclesInRange(0d, 0d, 100d).size(), 2);
        assertEquals(env.getObstaclesInRange(0d, 0d, 1d).size(), 1);
        assertEquals(env.getObstaclesInRange(0d, 0d, 1d).get(0), R1021);
        assertEquals(env.getObstaclesInRange(1d, 5d, 1d).size(), 1);
        assertEquals(env.getObstaclesInRange(1d, 5d, 1d).get(0), R0527);
        // CHECKSTYLE: MagicNumber ON
        assertEquals(env.getObstaclesInRange(0d, 0d, 0.5d).size(), 0);
    }

    private static class DummyNode extends GenericNode<Integer> {
        private static final long serialVersionUID = -6826365559224388894L;
        protected DummyNode(final Environment<Integer> env) {
            super(env);
        }
        @Override
        protected Integer createT() {
            return 0;
        }
        @Override
        public Map<Molecule, Integer> getContents() {
            return null;
        }
    }

}
