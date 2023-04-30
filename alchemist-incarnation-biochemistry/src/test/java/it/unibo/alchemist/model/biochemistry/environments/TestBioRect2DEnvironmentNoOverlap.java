/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.environments;

import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.core.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation;
import it.unibo.alchemist.model.biochemistry.CircularCellProperty;
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment;
import it.unibo.alchemist.model.linkingrules.NoLinks;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.kotest.assertions.FailKt.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
@SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
class TestBioRect2DEnvironmentNoOverlap {

    /**
     * The test seems to work for values of delta up to 2*10^(-8)
     * <p>
     * As future changes in the core can vary the sensibility of floating point operations, leading to an undesired
     * test failure, the value has been set to the next order of magnitude.
     */
    private static final double DELTA = 1e-7;
    private static final double BIG_CELL_DIAMETER = 30;
    private static final double MEDIUM_CELL_DIAMETER = 20;
    private static final double LITTLE_CELL_DIAMETER = 10;
    private static final BiochemistryIncarnation INCARNATION = new BiochemistryIncarnation();
    private static final Euclidean2DPosition POSITION_TO_MOVE1 = new Euclidean2DPosition(80, 0);
    private static final Euclidean2DPosition EXPECTED_POS1 = new Euclidean2DPosition(30, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE2 = POSITION_TO_MOVE1;
    private static final Euclidean2DPosition EXPECTED_POS2 =
            new Euclidean2DPosition(4 * 10 - FastMath.sqrt(75), 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE3 = new Euclidean2DPosition(80, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE4 = POSITION_TO_MOVE3;
    private static final Euclidean2DPosition POSITION_TO_MOVE5 = POSITION_TO_MOVE3;
    private static final Euclidean2DPosition POSITION_TO_MOVE6 = new Euclidean2DPosition(-80, 0);
    private static final Euclidean2DPosition EXPECTED_POS6 = new Euclidean2DPosition(-30, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE7 = new Euclidean2DPosition(80, 80);
    private static final Euclidean2DPosition EXPECTED_POS7 =
            new Euclidean2DPosition(40 - (10 / FastMath.sqrt(2)), 40 - (10 / FastMath.sqrt(2)));
    private static final Euclidean2DPosition POSITION_TO_MOVE8 = new Euclidean2DPosition(-80, -80);
    private static final Euclidean2DPosition EXPECTED_POS8 =
            new Euclidean2DPosition(-40 + 10 / FastMath.sqrt(2), -40 + 10 / FastMath.sqrt(2));
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM3_1 = new Euclidean2DPosition(5, 0);
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM3_2 = new Euclidean2DPosition(10, 0);
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM4_1 = new Euclidean2DPosition(-5, 0);
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM4_2 = new Euclidean2DPosition(-10, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE_TWOSTEP1 = new Euclidean2DPosition(50, 0);
    private static final Euclidean2DPosition EXPECTED_POS_TWOSTEP1_1 = new Euclidean2DPosition(40, 0);
    private static final Euclidean2DPosition EXPECTED_POS_TWOSTEP1_2 = new Euclidean2DPosition(90, 0);
    private static final Euclidean2DPosition NODE_POS9 = new Euclidean2DPosition(4, -5);
    private static final Euclidean2DPosition NODE_POS10 =
            new Euclidean2DPosition(2.813191105618545, 0.3019562530593296);
    private static final Euclidean2DPosition NODE_POS11_1 =
            new Euclidean2DPosition(1.2915251125665559, 1.7945837966921097);
    private static final Euclidean2DPosition NODE_POS11_2 =
            new Euclidean2DPosition(4.773603784764428, 0.23619996027968504);
    private static final Euclidean2DPosition NODE_POS11_3 =
            new Euclidean2DPosition(0.16085716189097174, 0.04968203900319437);
    private static final Euclidean2DPosition NODE_POS11_4 =
            new Euclidean2DPosition(3.122374292470004, -0.6490462479722794);
    private static final Euclidean2DPosition NODE_POS12_1 =
            new Euclidean2DPosition(1.2915251125665559, 1.7945837966921097);
    private static final Euclidean2DPosition NODE_POS12_2 =
            new Euclidean2DPosition(4.773603784764428, 0.23619996027968504);
    private static final Euclidean2DPosition NODE_POS12_3 =
            new Euclidean2DPosition(0.16085716189097174, 0.04968203900319437);
    private static final Euclidean2DPosition NODE_POS12_4 =
            new Euclidean2DPosition(3.122374292470004, -0.6490462479722794);
    private static final Euclidean2DPosition NODE_POS13_1 = new Euclidean2DPosition(5, 5);
    private static final Euclidean2DPosition NODE_POS13_2 = new Euclidean2DPosition(-5, 5);
    private static final Euclidean2DPosition NODE_POS13_3 = new Euclidean2DPosition(-5, -5);
    private static final Euclidean2DPosition NODE_POS13_4 = new Euclidean2DPosition(5, -5);
    private final Euclidean2DPosition originalPos = new Euclidean2DPosition(0, 0);
    private Node<Double> ng1;
    private Node<Double> ng2;
    private Node<Double> ng3;
    private Node<Double> nm1;
    private Node<Double> nm2;
    private Node<Double> np1;
    private Node<Double> np2;
    private Node<Double> np3;

    private Euclidean2DEnvironment<Double> environment;

    private Node<Double> createNode(final double diameter) {
        return environment.getIncarnation().createNode(new MersenneTwister(), environment, Double.toString(diameter));
    }

    /**
     *
     */
    @BeforeEach
    void setUp() {
        environment = new BioRect2DEnvironmentNoOverlap(INCARNATION);
        environment.setLinkingRule(new NoLinks<>());
        ng1 = createNode(BIG_CELL_DIAMETER);
        ng2 = createNode(BIG_CELL_DIAMETER);
        ng3 = createNode(BIG_CELL_DIAMETER);
        nm1 = createNode(MEDIUM_CELL_DIAMETER);
        nm2 = createNode(MEDIUM_CELL_DIAMETER);
        np1 = createNode(LITTLE_CELL_DIAMETER);
        np2 = createNode(LITTLE_CELL_DIAMETER);
        np3 = createNode(LITTLE_CELL_DIAMETER);
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    void testAddNode() {
        final Node<Double> n1 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n2 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n3 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n4 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n5 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n6 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n7 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n8 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n9 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n10 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> n11 = createNode(LITTLE_CELL_DIAMETER);

        final Euclidean2DPosition p2 = new Euclidean2DPosition(10, 0);
        environment.addNode(n1, originalPos);
        environment.addNode(n2, p2);

        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 20); // this should be added
        environment.addNode(n3, p3);
        assertEquals(environment.getPosition(n3), p3, getFailureTestString("n3", n3, p3));
        environment.removeNode(n3);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(0, 10); // this should be added
        environment.addNode(n4, p4);
        verifyAdded(n4, p4);
        environment.removeNode(n4);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(0, 5); // this should not be added
        environment.addNode(n5, p5);
        verifyNotAdded(n5);
        final Euclidean2DPosition p6 = new Euclidean2DPosition(5, 0); // this should not be added
        environment.addNode(n6, p6);
        verifyNotAdded(n6);
        final Euclidean2DPosition p7 = new Euclidean2DPosition(0, 0); // this should not be added
        environment.addNode(n7, p7);
        verifyNotAdded(n7);
        final Euclidean2DPosition p8 = new Euclidean2DPosition(10, 0); // this should not be added
        environment.addNode(n8, p8);
        verifyNotAdded(n8);
        final Euclidean2DPosition p9 = new Euclidean2DPosition(20, 0); // this should be added
        environment.addNode(n9, p9);
        verifyAdded(n9, p9);
        environment.removeNode(n9);
        final Euclidean2DPosition p10 = new Euclidean2DPosition(2.5, 2.5); // this should not be added
        environment.addNode(n10, p10);
        verifyNotAdded(n10);
        final Euclidean2DPosition p11 = new Euclidean2DPosition(7.5, -2.5); // this should not be added
        environment.addNode(n11, p11);
        verifyNotAdded(n11);
        environment.removeNode(n1);
        environment.removeNode(n2);
    }

    private void verifyNotAdded(final Node<Double> node) {
        assertFalse(environment.getNodes().contains(node));
    }

    private void verifyAdded(final Node<Double> node, @Nonnull final Position<?> expected) {
        final Position<?> position = environment.getPosition(node);
        final Supplier<String> message = () -> getFailureTestString("node" + node.getId(), node, position);
        assertEquals(expected, position, message);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode1() {
        final Node<Double> cellToMove1 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove1, originalPos);
        final Euclidean2DPosition p1 = new Euclidean2DPosition(40, 0);
        final Node<Double> c1 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c1, p1);
        environment.moveNode(cellToMove1, POSITION_TO_MOVE1);
        assertEquals(
                environment.getPosition(cellToMove1),
                EXPECTED_POS1,
                "cellToMove1 is in position: " + environment.getPosition(cellToMove1)
        );
        environment.removeNode(cellToMove1);
        environment.removeNode(c1);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode2() {
        final Node<Double> cellToMove2 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove2, originalPos);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(40, 5);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(40, -5);
        final Node<Double> c2 = createNode(LITTLE_CELL_DIAMETER);
        final Node<Double> c3 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c2, p2);
        environment.addNode(c3, p3);
        environment.moveNode(cellToMove2, POSITION_TO_MOVE2);
        assertEquals(
                environment.getPosition(cellToMove2),
                EXPECTED_POS2,
                "cellToMove2 is in position: " + environment.getPosition(cellToMove2)
        );
        environment.removeNode(cellToMove2);
        environment.removeNode(c2);
        environment.removeNode(c3);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode3() {
        final Node<Double> cellToMove3 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove3, originalPos);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(10, 0);
        final Node<Double> c4 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c4, p4);
        environment.moveNode(cellToMove3, POSITION_TO_MOVE3);
        assertEquals(
                environment.getPosition(cellToMove3),
                originalPos,
                "cellToMove3 is in position: " + environment.getPosition(cellToMove3)
        );
        environment.removeNode(cellToMove3);
        environment.removeNode(c4);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode4() {
        final Node<Double> cellToMove4 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove4, originalPos);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(
                0.2,
                FastMath.sqrt(FastMath.pow(cellToMove4
                        .asProperty(CircularCellProperty.class).getDiameter(), 2) - FastMath.pow(0.2, 2))
        );
        final Node<Double> c5 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c5, p5);
        environment.moveNode(cellToMove4, POSITION_TO_MOVE4);
        assertNotEquals(
                environment.getPosition(cellToMove4),
                POSITION_TO_MOVE4,
                "cellToMove4 is in position: " + environment.getPosition(cellToMove4)
        );
        environment.removeNode(cellToMove4);
        environment.removeNode(c5);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode5() {
        final Node<Double> cellToMove5 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove5, originalPos);
        final Euclidean2DPosition p6 = new Euclidean2DPosition(20, 10);
        final Node<Double> c6 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c6, p6);
        environment.moveNode(cellToMove5, POSITION_TO_MOVE5);
        assertEquals(
                environment.getPosition(cellToMove5),
                POSITION_TO_MOVE5,
                "cellToMove5 is in position: " + environment.getPosition(cellToMove5)
        );
        environment.removeNode(cellToMove5);
        environment.removeNode(c6);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode6() {
        final Node<Double> cellToMove6 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove6, originalPos);
        final Euclidean2DPosition p7 = new Euclidean2DPosition(-40, 0);
        final Node<Double> c7 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c7, p7);
        environment.moveNode(cellToMove6, POSITION_TO_MOVE6);
        assertEquals(
                environment.getPosition(cellToMove6),
                EXPECTED_POS6,
                "cellToMove6 is in position: " + environment.getPosition(cellToMove6)
        );
        environment.removeNode(cellToMove6);
        environment.removeNode(c7);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode7() {
        final Node<Double> cellToMove7 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove7, originalPos);
        final Euclidean2DPosition p8 = new Euclidean2DPosition(40, 40);
        final Node<Double> c8 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c8, p8);
        environment.moveNode(cellToMove7, POSITION_TO_MOVE7);
        assertTrueJUnit4("cellToMove7 is in position: " + environment.getPosition(cellToMove7),
                EXPECTED_POS7.equals(environment.getPosition(cellToMove7)));
        environment.removeNode(cellToMove7);
        environment.removeNode(c8);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode8() {
        final Node<Double> cellToMove8 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(cellToMove8, originalPos);
        final Euclidean2DPosition p9 = new Euclidean2DPosition(-40, -40);
        final Node<Double> c9 = createNode(LITTLE_CELL_DIAMETER);
        environment.addNode(c9, p9);
        environment.moveNode(cellToMove8, POSITION_TO_MOVE8);
        assertTrueJUnit4("cellToMove8 is in position: " + environment.getPosition(cellToMove8),
                EXPECTED_POS8.equals(environment.getPosition(cellToMove8)));
        environment.removeNode(cellToMove8);
        environment.removeNode(c9);
    }

    /*
     *  Here begins a section of the test class where the interactions between cell of different diameters are tested
     */

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    void testAddDifferentDiam1() {
        environment.addNode(ng1, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(10, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(0, 10);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(-10, -10);
        environment.addNode(np1, p1);
        environment.addNode(np2, p2);
        environment.addNode(np3, p3);
        nodeNotInEnvironment(environment, np1);
        nodeNotInEnvironment(environment, np2);
        nodeNotInEnvironment(environment, np3);
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    void testAddDifferentDiam2() {
        environment.addNode(np1, originalPos);
        final Euclidean2DPosition p1 = new Euclidean2DPosition(10, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(20, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(30, 0);
        environment.addNode(ng1, p1);
        environment.addNode(ng2, p2);
        environment.addNode(ng3, p3);
        nodeNotInEnvironment(environment, ng1);
        assertTrueJUnit4(
            getFailureTestString("ng2", ng2, p2),
            environment.getPosition(ng2).equals(p2)
        );
        nodeNotInEnvironment(environment, ng3);
        environment.removeNode(ng2);
        environment.addNode(ng3, p3);
        assertTrueJUnit4(
            getFailureTestString("ng3", ng3, p3),
            environment.getPosition(ng3).equals(p3)
        );
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    void testAddDifferentDiam3() {
        environment.addNode(np1, originalPos);
        final Euclidean2DPosition p1 = new Euclidean2DPosition(20, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(0, 15);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(10, 10);
        environment.addNode(ng1, p1);
        environment.addNode(nm1, p2);
        environment.addNode(nm2, p3);
        environment.addNode(np2, p3);
        assertTrueJUnit4(
            getFailureTestString("ng1", ng1, p1),
            environment.getPosition(ng1).equals(p1)
        );
        assertTrueJUnit4(
            getFailureTestString("nm1", nm1, p2),
            environment.getPosition(nm1).equals(p2)
        );
        nodeNotInEnvironment(environment, nm2);
        nodeNotInEnvironment(environment, np2);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam1() {
        final Node<Double> cellToMove1 = np1;
        environment.addNode(cellToMove1, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        environment.addNode(ng1, p1);
        environment.moveNode(cellToMove1, pd);
        assertTrueJUnit4("cellToMove1 is in position: " + environment.getPosition(cellToMove1),
                environment.getPosition(cellToMove1).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam2() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);
        final Node<Double> cellToMove2 = np2;
        environment.addNode(cellToMove2, originalPos);
        final Node<Double> bce = createNode(50);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(25, 30);
        environment.addNode(bce, p2);
        environment.moveNode(cellToMove2, pd);
        assertTrueJUnit4("cellToMove2 is in position: " + environment.getPosition(cellToMove2),
                environment.getPosition(cellToMove2).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam3() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);
        final Node<Double> cellToMove3 = np3;
        environment.addNode(cellToMove3, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 0);
        environment.addNode(ng1, p1);
        environment.moveNode(cellToMove3, pd);
        assertTrueJUnit4(
            "cellToMove3 is in position: " + environment.getPosition(cellToMove3),
            EXPECTED_POS_DIFFDIAM3_1.equals(environment.getPosition(cellToMove3))
        );
        environment.removeNode(ng1);
        environment.addNode(nm1, p1);
        environment.moveNode(cellToMove3, pd);
        assertTrueJUnit4(
            "cellToMove3 is in position: " + environment.getPosition(cellToMove3),
            EXPECTED_POS_DIFFDIAM3_2.equals(environment.getPosition(cellToMove3))
        );
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam4() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(-50, 0);
        final Node<Double> cellToMove4 = np3;
        environment.addNode(cellToMove4, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(-25, 0);
        environment.addNode(ng1, p1);
        environment.moveNode(cellToMove4, pd);
        assertTrueJUnit4(
            "cellToMove4 is in position: " + environment.getPosition(cellToMove4),
            EXPECTED_POS_DIFFDIAM4_1.equals(environment.getPosition(cellToMove4))
        );
        environment.removeNode(ng1);
        environment.addNode(nm1, p1);
        environment.moveNode(cellToMove4, pd);
        assertTrueJUnit4(
            "cellToMove4 is in position: " + environment.getPosition(cellToMove4),
            EXPECTED_POS_DIFFDIAM4_2.equals(environment.getPosition(cellToMove4))
        );
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam5() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 50);
        final Node<Double> cellToMove5 = np3;
        environment.addNode(cellToMove5, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 25);
        environment.addNode(ng1, p1);
        environment.moveNode(cellToMove5, pd);
        assertNotEquals(
                environment.getPosition(cellToMove5),
                pd,
                "cellToMove5 is in position: " + environment.getPosition(cellToMove5)
        );
        environment.removeNode(ng1);
        environment.addNode(nm1, p1);
        environment.moveNode(cellToMove5, pd);
        assertNotEquals(
                environment.getPosition(cellToMove5),
                pd,
                "cellToMove5 is in position: " + environment.getPosition(cellToMove5)
        );
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam6() {
        final Node<Double> cellToMove6 = np1;
        environment.addNode(cellToMove6, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        environment.addNode(ng1, p1);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(-10, 0);
        environment.addNode(np2, p2);
        environment.moveNode(cellToMove6, pd);
        assertTrueJUnit4(
                "cellToMove6 is in position: " + environment.getPosition(cellToMove6),
                environment.getPosition(cellToMove6).equals(pd)
        );
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam7() {
        final Node<Double> cellToMove7 = np1;
        environment.addNode(cellToMove7, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        environment.addNode(ng1, p1);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(60, 5);
        environment.addNode(np2, p2);
        environment.moveNode(cellToMove7, pd);
        assertTrueJUnit4("cellToMove7 is in position: " + environment.getPosition(cellToMove7),
                environment.getPosition(cellToMove7).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveDifferentDiam8() {
        final Node<Double> cellToMove8 = np1;
        environment.addNode(cellToMove8, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        environment.addNode(ng1, p1);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(0, 10);
        environment.addNode(np2, p2);
        environment.moveNode(cellToMove8, pd);
        assertTrueJUnit4("cellToMove8 is in position: " + environment.getPosition(cellToMove8),
                environment.getPosition(cellToMove8).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others and if moving two times the cell the neighborhood is
     * correctly updated.
     */
    @Test
    void testMoveInTwoSteps1() {
        final Node<Double> c1 = np1;
        environment.addNode(c1, originalPos);
        final Euclidean2DPosition pd1 = new Euclidean2DPosition(50, 0);
        final Euclidean2DPosition pd2 = new Euclidean2DPosition(100, 0);
        final Node<Double> c2 = np2;
        environment.addNode(c2, pd1);
        environment.moveNode(c1, POSITION_TO_MOVE_TWOSTEP1);
        assertEquals(EXPECTED_POS_TWOSTEP1_1, environment.getPosition(c1), "c1 is in pos : " + environment.getPosition(c1));
        environment.moveNode(c2, pd1);
        environment.moveNodeToPosition(c1, pd2);
        assertEquals(EXPECTED_POS_TWOSTEP1_2, environment.getPosition(c1), "c1 is in pos : " + environment.getPosition(c1));

    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode9() {
        final Node<Double> c1 = createNode(1);
        final Node<Double> c2 = createNode(1);
        environment.addNode(c2, NODE_POS9);
        environment.addNode(c1, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(4.737000465393066, -5.0);
        environment.moveNode(c1, pd);
        assertNotEquals(environment.getPosition(c1), pd);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode10() {
        final Node<Double> c1 = createNode(1);
        final Node<Double> c2 = createNode(1);
        environment.addNode(c2, NODE_POS10);
        environment.addNode(c1, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(3.122374292470004, -0.6490462479722794);
        environment.moveNode(c1, pd);
        assertNotEquals(environment.getPosition(c1), pd);
    }

    private List<Node<Double>> getOverlappingNodes(final Node<Double> node) {
        final double diameter = node.asProperty(CircularCellProperty.class).getDiameter();
        return  environment.getNodesWithinRange(node, diameter).stream()
                .filter(n -> environment.getDistanceBetweenNodes(node, n) < diameter)
                .collect(Collectors.toList());
    }

    private List<String> mapToNodePositions(final List<Node<Double>> nodes) {
        return nodes.stream().map(node -> environment.getPosition(node).toString()).collect(Collectors.toList());
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode11() {
        final double diameter = 1;
        final Node<Double> c1 = createNode(diameter);
        final Node<Double> c2 = createNode(diameter);
        final Node<Double> c3 = createNode(diameter);
        final Node<Double> c4 = createNode(diameter);
        environment.addNode(c1, NODE_POS11_1);
        environment.addNode(c2, NODE_POS11_2);
        environment.addNode(c3, NODE_POS11_3);
        environment.addNode(c4, NODE_POS11_4);
        final Euclidean2DPosition pd = new Euclidean2DPosition(5.0, -1.8431210525510544);
        environment.moveNodeToPosition(c1, pd);
        assertTrueJUnit4("Should be empty but is : " + mapToNodePositions(getOverlappingNodes(c1)),
                environment.getNodesWithinRange(c1, diameter - DELTA).isEmpty());
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode12() {
        final double diameter = 1;
        final Node<Double> c1 = createNode(diameter);
        final Node<Double> c2 = createNode(diameter);
        final Node<Double> c3 = createNode(diameter);
        final Node<Double> c4 = createNode(diameter);
        environment.addNode(c1, NODE_POS12_1);
        environment.addNode(c2, NODE_POS12_2);
        environment.addNode(c3, NODE_POS12_3);
        environment.addNode(c4, NODE_POS12_4);
        final Euclidean2DPosition pd = new Euclidean2DPosition(5.3, -1.8431210525510544);
        environment.moveNodeToPosition(c1, pd);
        assertTrueJUnit4(
                "Should be empty but is : " + mapToNodePositions(getOverlappingNodes(c1)),
                environment.getNodesWithinRange(c1, diameter).isEmpty()
        );
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    void testMoveNode13() {
        final double diameter = 1;
        final Node<Double> c1 = createNode(diameter);
        final Node<Double> c2 = createNode(diameter);
        final Node<Double> c3 = createNode(diameter);
        final Node<Double> c4 = createNode(diameter);
        environment.addNode(c1, NODE_POS13_1);
        environment.addNode(c2, NODE_POS13_2);
        environment.addNode(c3, NODE_POS13_3);
        environment.addNode(c4, NODE_POS13_4);
        final Euclidean2DPosition pd = new Euclidean2DPosition(10, 10);
        environment.moveNodeToPosition(c1, pd);
        environment.moveNodeToPosition(c2, pd);
        environment.moveNodeToPosition(c3, pd);
        environment.moveNodeToPosition(c4, pd);
        assertTrueJUnit4("Should be empty but is : " + mapToNodePositions(getOverlappingNodes(c1)),
                environment.getNodesWithinRange(c1, diameter).isEmpty());
    }

    /**
     * Test in a simulation if there's no overlapping between cells.
     */
    @Test
    void testNoOverlapInSimulation1() {
        testLoading("provaBCReaction.yml");
    }

    /**
     * Test in a simulation if there's no overlapping between cells.
     */
    @Test
    void testNoOverlapInSimulation2() {
        testLoading("provaBCReaction2.yml");
    }

    private static void testLoading(final String resource) {
        final Map<String, Double> vars = Collections.emptyMap();
        final var res = ResourceLoader.getResource(resource);
        assertNotNull(res, "Missing test resource " + resource);
        final Environment<Double, Euclidean2DPosition> env = LoadAlchemist.from(res)
                .<Double, Euclidean2DPosition>getWith(vars)
                .getEnvironment();
        final Simulation<Double, Euclidean2DPosition> sim = new Engine<>(env, 10000);
        sim.addOutputMonitor(new OutputMonitor<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void stepDone(
                    @Nonnull final Environment<Double, Euclidean2DPosition> environment,
                    final Actionable<Double> reaction,
                    @Nonnull final Time time,
                    final long step
            ) {
                assertTrue(thereIsOverlap(environment), "Fail at time: " + time);
            }

            @Override
            public void initialized(@Nonnull final Environment<Double, Euclidean2DPosition> environment) {
                assertTrue(thereIsOverlap(environment));
            }

            @Override
            public void finished(
                    @Nonnull final Environment<Double, Euclidean2DPosition> environment,
                    @Nonnull final Time time,
                    final long step
            ) {
                assertTrue(thereIsOverlap(environment));
            }

            private Stream<Node<Double>> getNodes() {
                return env.getNodes().stream().filter(n -> n.asPropertyOrNull(CircularCellProperty.class) != null);
            }

            private boolean thereIsOverlap(final Environment<Double, Euclidean2DPosition> env) {
                getNodes().flatMap(n -> getNodes()
                                .filter(c -> !c.equals(n))
                                .filter(c -> env.getDistanceBetweenNodes(n, c)
                                        < n.asProperty(CircularCellProperty.class).getRadius()
                                        + c.asProperty(CircularCellProperty.class).getRadius() - DELTA)
                                .map(c -> new Pair<>(n, c)))
                        .findAny()
                        .ifPresent(e -> fail("Nodes " + e.getFirst().getId()
                                + env.getPosition(e.getFirst()) + " and "
                                + e.getSecond().getId() + env.getPosition(e.getSecond()) + " are overlapping. "
                                + "Their distance is: " + env.getDistanceBetweenNodes(e.getFirst(), e.getSecond())
                                + " but should be greater than "
                                + (e.getFirst().asProperty(CircularCellProperty.class).getRadius()
                                + e.getSecond().asProperty(CircularCellProperty.class).getRadius())));
                return true;
            }
        });
        sim.play();
        sim.run();
        sim.getError().ifPresent(CheckedConsumer.unchecked(it -> {
            throw it;
        }));
    }

    private String getFailureTestString(final String nodeName, final Node<Double> n, final Position<?> expected) {
        return nodeName + " not in pos " + expected + "; it's in pos " + environment.getPosition(n);
    }

    private static <T> void nodeNotInEnvironment(final Environment<T, ?> environment, final Node<T> node) {
        checkNodeInEnvironment(environment, node, false);
    }

    private static <T> void checkNodeInEnvironment(
        final Environment<T, ?> environment,
        final Node<T> node,
        final boolean shouldBePresent
    ) {
        final var isInEnvironment = environment.getNodes().contains(node);
        if (shouldBePresent) {
            assertTrue(isInEnvironment, () -> "node " + node + " is not in the environment");
            final var position = environment.getPosition(node);
            assertNotNull(position, () -> "node " + node + " has no valid position");
        } else {
            assertFalse(
                isInEnvironment,
                () -> "node " + node + " is in the environment at position " + environment.getPosition(node)
            );
        }
    }


    private static void assertTrueJUnit4(final String msg, final boolean res) {
        assertTrue(res, msg);
    }
}
