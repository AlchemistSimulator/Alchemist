/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * 
 */
public class TestChemotaxis {

    private static final double CONCENTRATION1 = 5;
    private static final double CONCENTRATION2 = 10;
    private static final double CONCENTRATION3 = 1;
    private static final String CHEMIOTACTIC_POLARIZATION_REACTION = "[] --> [ChemotacticPolarization(A, up)]";
    private static final int EXPECTED_NODES = 5;
    private static final double PRECISION = 0.000000000000001;
    private static final String CELL_MOVE_REACTION = "[] --> [CellMove(false, 1)]";
    private Environment<Double, Euclidean2DPosition> env;
    private CellNode<Euclidean2DPosition> cellNode1;
    private EnvironmentNode envNode1;
    private EnvironmentNode envNode2;
    private EnvironmentNode envNode3;
    private EnvironmentNode envNode4;
    private final Biomolecule biomolA = new Biomolecule("A");
    private final Incarnation<Double, Euclidean2DPosition> inc = new BiochemistryIncarnation<>();
    private RandomGenerator rand;
    private TimeDistribution<Double> time;

    /**
     * 
     */
    @BeforeEach
    public void setUp() {
        env = new BioRect2DEnvironmentNoOverlap();
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        envNode1 = new EnvironmentNodeImpl(env);
        envNode2 = new EnvironmentNodeImpl(env);
        envNode3 = new EnvironmentNodeImpl(env);
        envNode4 = new EnvironmentNodeImpl(env);
        cellNode1 = new CellNodeImpl<>(env);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    public void testChemotacticPolarization1() {
        final Euclidean2DPosition p1 =  new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 =  new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 =  new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 =  new Euclidean2DPosition(0.5, 0.5);
        env.addNode(envNode1, p1);
        env.addNode(envNode2, p2);
        env.addNode(envNode3, p3);
        env.addNode(envNode4, p4);
        env.addNode(cellNode1, p5);
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION); 
        r.execute();
        assertEquals(cellNode1.getPolarizationVersor().getX(),
                FastMath.sqrt(0.5),
                PRECISION
                );
        assertEquals(cellNode1.getPolarizationVersor().getY(),
                FastMath.sqrt(0.5),
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    public void testChemotacticPolarization2() {
        final Euclidean2DPosition p1 =  new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 =  new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 =  new Euclidean2DPosition(3, 3);
        final Euclidean2DPosition p5 =  new Euclidean2DPosition(0.5, 0.5);
        env.addNode(envNode1, p1);
        env.addNode(envNode2, p2);
        env.addNode(envNode3, p3);
        env.addNode(envNode4, p4);
        env.addNode(cellNode1, p5);
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION);
        r.execute();
        assertEquals(cellNode1.getPolarizationVersor().getX(),
                0,
                PRECISION
                );
        assertEquals(cellNode1.getPolarizationVersor().getY(),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    public void testChemotacticPolarization3() {
        final Euclidean2DPosition p1 =  new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 =  new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 =  new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 =  new Euclidean2DPosition(0.5, 0.5);
        env.addNode(envNode1, p1);
        env.addNode(envNode2, p2);
        env.addNode(envNode3, p3);
        env.addNode(envNode4, p4);
        env.addNode(cellNode1, p5);
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION);
        r.execute();
        assertEquals(cellNode1.getPolarizationVersor().getX(),
                0,
                PRECISION
                );
        assertEquals(cellNode1.getPolarizationVersor().getY(),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    public void testChemotacticPolarization4() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0.5, 0.5);
        env.addNode(cellNode1, p1);
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION);
        r.execute();
        assertEquals(cellNode1.getPolarizationVersor().getX(),
                0,
                PRECISION
                );
        assertEquals(cellNode1.getPolarizationVersor().getY(),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    public void testChemotacticPolarization5() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(-1, 0);
        env.addNode(cellNode1, p1);
        final Molecule a = new Biomolecule("A");
        final Molecule b = new Biomolecule("B");
        envNode1.setConcentration(a, CONCENTRATION3);
        envNode2.setConcentration(b, CONCENTRATION3);
        env.addNode(envNode1, p2);
        env.addNode(envNode2, p3);
        final Reaction<Double> r1 = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION);
        final Reaction<Double> r2 = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemotacticPolarization(B, up)]");
        r1.execute();
        r2.execute();
        assertEquals(cellNode1.getPolarizationVersor().getX(),
                0,
                PRECISION
                );
        assertEquals(cellNode1.getPolarizationVersor().getY(),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell moves according to the given polarization.
     */
    @Test
    public void testChemotacticMove1() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(0.5, 0.5);
        env.addNode(envNode1, p1);
        env.addNode(envNode2, p2);
        env.addNode(envNode3, p3);
        env.addNode(envNode4, p4);
        env.addNode(cellNode1, p5);
        assertEquals(EXPECTED_NODES, env.getNodesNumber());
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r1 = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION);
        final Reaction<Double> r2 = inc.createReaction(rand, env, cellNode1, time, CELL_MOVE_REACTION);
        r1.execute();
        r2.execute();
        assertEquals(new Euclidean2DPosition(0.5 + FastMath.sqrt(0.5), 0.5 + FastMath.sqrt(0.5)), env.getPosition(cellNode1));
    }

    /**
     * Testing if cell moves according to the given polarization.
     */
    @Test
    public void testChemotacticMove2() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(1, 1);
        env.addNode(envNode1, p1);
        env.addNode(envNode2, p2);
        env.addNode(envNode3, p3);
        env.addNode(envNode4, p4);
        env.addNode(cellNode1, p5);
        assertEquals(EXPECTED_NODES, env.getNodesNumber());
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r1 = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION);
        final Reaction<Double> r2 = inc.createReaction(rand, env, cellNode1, time, CELL_MOVE_REACTION);
        r1.execute();
        r2.execute();
        assertEquals(1,
                env.getPosition(cellNode1).getX(),
                PRECISION
                );
        assertEquals(1,
                env.getPosition(cellNode1).getY(),
                PRECISION
                );
    }

    /**
     * Testing if cell moves according to the given polarization.
     */
    @Test
    public void testChemotacticMove3() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(0.5, 0.5);
        env.addNode(envNode1, p1);
        env.addNode(envNode2, p2);
        env.addNode(envNode3, p3);
        env.addNode(envNode4, p4);
        env.addNode(cellNode1, p5);
        assertEquals(EXPECTED_NODES, env.getNodesNumber());
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION2);
        envNode3.setConcentration(biomolA, CONCENTRATION2);
        envNode1.setConcentration(biomolA, CONCENTRATION2);
        final Reaction<Double> r1 = inc.createReaction(rand, env, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION);
        final Reaction<Double> r2 = inc.createReaction(rand, env, cellNode1, time, CELL_MOVE_REACTION);
        r1.execute();
        r2.execute();
        r2.execute();
        assertEquals(0.5,
                env.getPosition(cellNode1).getX(),
                PRECISION
                );
        assertEquals(0.5,
                env.getPosition(cellNode1).getY(),
                PRECISION
                );
    }
}
