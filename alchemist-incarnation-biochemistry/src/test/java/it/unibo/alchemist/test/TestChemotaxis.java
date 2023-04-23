/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.EnvironmentNode;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.interfaces.properties.CircularCellProperty;
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("CPD-START")
class TestChemotaxis {

    private static final double CONCENTRATION1 = 5;
    private static final double CONCENTRATION2 = 10;
    private static final double CONCENTRATION3 = 1;
    private static final String CHEMIOTACTIC_POLARIZATION_REACTION = "[] --> [ChemotacticPolarization(A, up)]";
    private static final int EXPECTED_NODES = 5;
    private static final double PRECISION = 1e-15;
    private static final String CELL_MOVE_REACTION = "[] --> [CellMove(false, 1)]";
    private Environment<Double, Euclidean2DPosition> environment;
    private Node<Double> cellNode1;
    private EnvironmentNode envNode1;
    private EnvironmentNode envNode2;
    private EnvironmentNode envNode3;
    private EnvironmentNode envNode4;
    private final Biomolecule biomolA = new Biomolecule("A");
    private final BiochemistryIncarnation incarnation = new BiochemistryIncarnation();
    private RandomGenerator rand;
    private TimeDistribution<Double> time;

    /**
     * 
     */
    @BeforeEach
    public void setUp() {
        environment = new BioRect2DEnvironmentNoOverlap(incarnation);
        environment.setLinkingRule(
                new ConnectWithinDistance<>(2)
        );
        envNode1 = new EnvironmentNodeImpl(environment);
        envNode2 = new EnvironmentNodeImpl(environment);
        envNode3 = new EnvironmentNodeImpl(environment);
        envNode4 = new EnvironmentNodeImpl(environment);
        cellNode1 = incarnation.createNode(rand, environment, null);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    void testChemotacticPolarization1() {
        final Euclidean2DPosition p1 =  new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 =  new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 =  new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 =  new Euclidean2DPosition(0.5, 0.5);
        environment.addNode(envNode1, p1);
        environment.addNode(envNode2, p2);
        environment.addNode(envNode3, p3);
        environment.addNode(envNode4, p4);
        environment.addNode(cellNode1, p5);
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        r.execute();
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(0),
                FastMath.sqrt(0.5),
                PRECISION
                );
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(1),
                FastMath.sqrt(0.5),
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    void testChemotacticPolarization2() {
        final Euclidean2DPosition p1 =  new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 =  new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 =  new Euclidean2DPosition(3, 3);
        final Euclidean2DPosition p5 =  new Euclidean2DPosition(0.5, 0.5);
        environment.addNode(envNode1, p1);
        environment.addNode(envNode2, p2);
        environment.addNode(envNode3, p3);
        environment.addNode(envNode4, p4);
        environment.addNode(cellNode1, p5);
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        r.execute();
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(0),
                0,
                PRECISION
                );
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(1),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    void testChemotacticPolarization3() {
        final Euclidean2DPosition p1 =  new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 =  new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 =  new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 =  new Euclidean2DPosition(0.5, 0.5);
        environment.addNode(envNode1, p1);
        environment.addNode(envNode2, p2);
        environment.addNode(envNode3, p3);
        environment.addNode(envNode4, p4);
        environment.addNode(cellNode1, p5);
        final Reaction<Double> r = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        r.execute();
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(0),
                0,
                PRECISION
                );
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(1),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    void testChemotacticPolarization4() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0.5, 0.5);
        environment.addNode(cellNode1, p1);
        final Reaction<Double> r = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        r.execute();
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(0),
                0,
                PRECISION
                );
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(1),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell is polarized correctly.
     */
    @Test
    void testChemotacticPolarization5() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(-1, 0);
        environment.addNode(cellNode1, p1);
        final Molecule a = new Biomolecule("A");
        final Molecule b = new Biomolecule("B");
        envNode1.setConcentration(a, CONCENTRATION3);
        envNode2.setConcentration(b, CONCENTRATION3);
        environment.addNode(envNode1, p2);
        environment.addNode(envNode2, p3);
        final Reaction<Double> r1 = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        final Reaction<Double> r2 = incarnation.createReaction(
                rand, environment, cellNode1, time, "[] --> [ChemotacticPolarization(B, up)]"
        );
        r1.execute();
        r2.execute();
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(0),
                0,
                PRECISION
                );
        assertEquals(cellNode1.asProperty(CircularCellProperty.class).getPolarizationVersor().getCoordinate(1),
                0,
                PRECISION
                );
    }

    /**
     * Testing if cell moves according to the given polarization.
     */
    @Test
    void testChemotacticMove1() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(0.5, 0.5);
        environment.addNode(envNode1, p1);
        environment.addNode(envNode2, p2);
        environment.addNode(envNode3, p3);
        environment.addNode(envNode4, p4);
        environment.addNode(cellNode1, p5);
        assertEquals(EXPECTED_NODES, environment.getNodeCount());
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r1 = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        final Reaction<Double> r2 = incarnation.createReaction(rand, environment, cellNode1, time, CELL_MOVE_REACTION);
        r1.execute();
        r2.execute();
        assertEquals(new Euclidean2DPosition(0.5 + FastMath.sqrt(0.5), 0.5 + FastMath.sqrt(0.5)),
                environment.getPosition(cellNode1)
        );
    }

    /**
     * Testing if cell moves according to the given polarization.
     */
    @Test
    void testChemotacticMove2() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(1, 1);
        environment.addNode(envNode1, p1);
        environment.addNode(envNode2, p2);
        environment.addNode(envNode3, p3);
        environment.addNode(envNode4, p4);
        environment.addNode(cellNode1, p5);
        assertEquals(EXPECTED_NODES, environment.getNodeCount());
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION1);
        envNode3.setConcentration(biomolA, CONCENTRATION1);
        final Reaction<Double> r1 = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        final Reaction<Double> r2 = incarnation.createReaction(rand, environment, cellNode1, time, CELL_MOVE_REACTION);
        r1.execute();
        r2.execute();
        assertEquals(1,
                environment.getPosition(cellNode1).getX(),
                PRECISION
                );
        assertEquals(1,
                environment.getPosition(cellNode1).getY(),
                PRECISION
                );
    }

    /**
     * Testing if cell moves according to the given polarization.
     */
    @Test
    void testChemotacticMove3() {
        final Euclidean2DPosition p1 = new Euclidean2DPosition(0, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(1, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 1);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(1, 1);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(0.5, 0.5);
        environment.addNode(envNode1, p1);
        environment.addNode(envNode2, p2);
        environment.addNode(envNode3, p3);
        environment.addNode(envNode4, p4);
        environment.addNode(cellNode1, p5);
        assertEquals(EXPECTED_NODES, environment.getNodeCount());
        envNode4.setConcentration(biomolA, CONCENTRATION2);
        envNode2.setConcentration(biomolA, CONCENTRATION2);
        envNode3.setConcentration(biomolA, CONCENTRATION2);
        envNode1.setConcentration(biomolA, CONCENTRATION2);
        final Reaction<Double> r1 = incarnation.createReaction(
                rand, environment, cellNode1, time, CHEMIOTACTIC_POLARIZATION_REACTION
        );
        final Reaction<Double> r2 = incarnation.createReaction(rand, environment, cellNode1, time, CELL_MOVE_REACTION);
        r1.execute();
        r2.execute();
        r2.execute();
        assertEquals(0.5,
                environment.getPosition(cellNode1).getX(),
                PRECISION
                );
        assertEquals(0.5,
                environment.getPosition(cellNode1).getY(),
                PRECISION
                );
    }
}
