/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.kotlintest.DslKt.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class TestBioRect2DEnvironmentNoOverlap {

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
    private static final Euclidean2DPosition POSITION_TO_MOVE1 = new Euclidean2DPosition(80, 0);
    private static final Euclidean2DPosition EXPECTED_POS1 = new Euclidean2DPosition(30, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE2 = POSITION_TO_MOVE1;
    private static final Euclidean2DPosition EXPECTED_POS2 = new Euclidean2DPosition(4 * 10 - FastMath.sqrt(75), 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE3 = new Euclidean2DPosition(80, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE4 = POSITION_TO_MOVE3;
    private static final Euclidean2DPosition POSITION_TO_MOVE5 = POSITION_TO_MOVE3;
    private static final Euclidean2DPosition POSITION_TO_MOVE6 = new Euclidean2DPosition(-80, 0);
    private static final Euclidean2DPosition EXPECTED_POS6 = new Euclidean2DPosition(-30, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE7 = new Euclidean2DPosition(80, 80);
    private static final Euclidean2DPosition EXPECTED_POS7 = new Euclidean2DPosition(40 - (10 / FastMath.sqrt(2)), 40 - (10 / FastMath.sqrt(2)));
    private static final Euclidean2DPosition POSITION_TO_MOVE8 = new Euclidean2DPosition(-80, -80);
    private static final Euclidean2DPosition EXPECTED_POS8 = new Euclidean2DPosition(-40 + 10 / FastMath.sqrt(2), -40 + 10 / FastMath.sqrt(2));
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM3_1 = new Euclidean2DPosition(5, 0);
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM3_2 = new Euclidean2DPosition(10, 0);
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM4_1 = new Euclidean2DPosition(-5, 0);
    private static final Euclidean2DPosition EXPECTED_POS_DIFFDIAM4_2 = new Euclidean2DPosition(-10, 0);
    private static final Euclidean2DPosition POSITION_TO_MOVE_TWOSTEP1 = new Euclidean2DPosition(50, 0);
    private static final Euclidean2DPosition EXPECTED_POS_TWOSTEP1_1 = new Euclidean2DPosition(40, 0);
    private static final Euclidean2DPosition EXPECTED_POS_TWOSTEP1_2 = new Euclidean2DPosition(90, 0);
    private static final Euclidean2DPosition NODE_POS9 = new Euclidean2DPosition(4, -5);
    private static final Euclidean2DPosition NODE_POS10 = new Euclidean2DPosition(2.813191105618545, 0.3019562530593296);
    private static final Euclidean2DPosition NODE_POS11_1 = new Euclidean2DPosition(1.2915251125665559, 1.7945837966921097);
    private static final Euclidean2DPosition NODE_POS11_2 = new Euclidean2DPosition(4.773603784764428, 0.23619996027968504);
    private static final Euclidean2DPosition NODE_POS11_3 = new Euclidean2DPosition(0.16085716189097174, 0.04968203900319437);
    private static final Euclidean2DPosition NODE_POS11_4 = new Euclidean2DPosition(3.122374292470004, -0.6490462479722794);
    private static final Euclidean2DPosition NODE_POS12_1 = new Euclidean2DPosition(1.2915251125665559, 1.7945837966921097);
    private static final Euclidean2DPosition NODE_POS12_2 = new Euclidean2DPosition(4.773603784764428, 0.23619996027968504);
    private static final Euclidean2DPosition NODE_POS12_3 = new Euclidean2DPosition(0.16085716189097174, 0.04968203900319437);
    private static final Euclidean2DPosition NODE_POS12_4 = new Euclidean2DPosition(3.122374292470004, -0.6490462479722794);
    private static final Euclidean2DPosition NODE_POS13_1 = new Euclidean2DPosition(5, 5);
    private static final Euclidean2DPosition NODE_POS13_2 = new Euclidean2DPosition(-5, 5);
    private static final Euclidean2DPosition NODE_POS13_3 = new Euclidean2DPosition(-5, -5);
    private static final Euclidean2DPosition NODE_POS13_4 = new Euclidean2DPosition(5, -5);
    private final Euclidean2DPosition originalPos = new Euclidean2DPosition(0, 0);
    private CellWithCircularArea<Euclidean2DPosition> ng1;
    private CellWithCircularArea<Euclidean2DPosition> ng2;
    private CellWithCircularArea<Euclidean2DPosition> ng3;
    private CellWithCircularArea<Euclidean2DPosition> nm1;
    private CellWithCircularArea<Euclidean2DPosition> nm2;
    private CellWithCircularArea<Euclidean2DPosition> np1;
    private CellWithCircularArea<Euclidean2DPosition> np2;
    private CellWithCircularArea<Euclidean2DPosition> np3;

    private Environment<Double, Euclidean2DPosition> env;

    /**
     *
     */
    @BeforeEach
    public void setUp() {
        env = new BioRect2DEnvironmentNoOverlap();
        env.setLinkingRule(new NoLinks<>());
        ng1 = new CellNodeImpl<>(env, BIG_CELL_DIAMETER);
        ng2 = new CellNodeImpl<>(env, BIG_CELL_DIAMETER);
        ng3 = new CellNodeImpl<>(env, BIG_CELL_DIAMETER);
        nm1 = new CellNodeImpl<>(env, MEDIUM_CELL_DIAMETER);
        nm2 = new CellNodeImpl<>(env, MEDIUM_CELL_DIAMETER);
        np1 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        np2 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        np3 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    public void testAddNode() {
        final CellWithCircularArea<Euclidean2DPosition> n1 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n2 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n3 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n4 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n5 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n6 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n7 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n8 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n9 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n10 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> n11 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);

        final Euclidean2DPosition p2 = new Euclidean2DPosition(10, 0);
        env.addNode(n1, originalPos);
        env.addNode(n2, p2);

        final Euclidean2DPosition p3 = new Euclidean2DPosition(0, 20); // this should be added
        env.addNode(n3, p3);
        assertEquals(env.getPosition(n3), p3, getFailureTestString("n3", n3, p3));
        env.removeNode(n3);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(0, 10); // this should be added
        env.addNode(n4, p4);
        verifyAdded(n4, p4);
        env.removeNode(n4);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(0, 5); // this should not be added
        env.addNode(n5, p5);
        verifyNotAdded(n5);
        final Euclidean2DPosition p6 = new Euclidean2DPosition(5, 0); // this should not be added
        env.addNode(n6, p6);
        verifyNotAdded(n6);
        final Euclidean2DPosition p7 = new Euclidean2DPosition(0, 0); // this should not be added
        env.addNode(n7, p7);
        verifyNotAdded(n7);
        final Euclidean2DPosition p8 = new Euclidean2DPosition(10, 0); // this should not be added
        env.addNode(n8, p8);
        verifyNotAdded(n8);
        final Euclidean2DPosition p9 = new Euclidean2DPosition(20, 0); // this should be added
        env.addNode(n9, p9);
        verifyAdded(n9, p9);
        env.removeNode(n9);
        final Euclidean2DPosition p10 = new Euclidean2DPosition(2.5, 2.5); // this should not be added
        env.addNode(n10, p10);
        verifyNotAdded(n10);
        final Euclidean2DPosition p11 = new Euclidean2DPosition(7.5, -2.5); // this should not be added
        env.addNode(n11, p11);
        verifyNotAdded(n11);
        env.removeNode(n1);
        env.removeNode(n2);
    }

    private void verifyNotAdded(final Node<Double> node) {
        verifyAdded(node, null);
    }

    private void verifyAdded(final Node<Double> node, final Position<?> expected) {
        final Position<?> position = env.getPosition(node);
        final Supplier<String> message = () -> getFailureTestString("node" + node.getId(), node, position);
        if (expected == null) {
            assertNull(position, message);
        } else {
            assertEquals(expected, position, message);
        }
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode1() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove1 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove1, originalPos);
        final Euclidean2DPosition p1 = new Euclidean2DPosition(40, 0);
        final CellWithCircularArea<Euclidean2DPosition> c1 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c1, p1);
        env.moveNode(cellToMove1, POSITION_TO_MOVE1);
        assertEquals(env.getPosition(cellToMove1), EXPECTED_POS1, "cellToMove1 is in position: " + env.getPosition(cellToMove1));
        env.removeNode(cellToMove1);
        env.removeNode(c1);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode2() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove2 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove2, originalPos);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(40, 5);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(40, -5);
        final CellWithCircularArea<Euclidean2DPosition> c2 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea<Euclidean2DPosition> c3 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c2, p2);
        env.addNode(c3, p3);
        env.moveNode(cellToMove2, POSITION_TO_MOVE2);
        assertEquals(env.getPosition(cellToMove2), EXPECTED_POS2, "cellToMove2 is in position: " + env.getPosition(cellToMove2).toString());
        env.removeNode(cellToMove2);
        env.removeNode(c2);
        env.removeNode(c3);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode3() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove3 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove3, originalPos);
        final Euclidean2DPosition p4 = new Euclidean2DPosition(10, 0);
        final CellWithCircularArea<Euclidean2DPosition> c4 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c4, p4);
        env.moveNode(cellToMove3, POSITION_TO_MOVE3);
        assertEquals(env.getPosition(cellToMove3), originalPos, "cellToMove3 is in position: " + env.getPosition(cellToMove3).toString());
        env.removeNode(cellToMove3);
        env.removeNode(c4);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode4() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove4 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove4, originalPos);
        final Euclidean2DPosition p5 = new Euclidean2DPosition(0.2, FastMath.sqrt(FastMath.pow(cellToMove4.getDiameter(), 2) - FastMath.pow(0.2, 2)));
        final CellWithCircularArea<Euclidean2DPosition> c5 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c5, p5);
        env.moveNode(cellToMove4, POSITION_TO_MOVE4);
        assertNotEquals(env.getPosition(cellToMove4), POSITION_TO_MOVE4, "cellToMove4 is in position: " + env.getPosition(cellToMove4).toString());
        env.removeNode(cellToMove4);
        env.removeNode(c5);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode5() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove5 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove5, originalPos);
        final Euclidean2DPosition p6 = new Euclidean2DPosition(20, 10);
        final CellWithCircularArea<Euclidean2DPosition> c6 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c6, p6);
        env.moveNode(cellToMove5, POSITION_TO_MOVE5);
        assertEquals(env.getPosition(cellToMove5), POSITION_TO_MOVE5, "cellToMove5 is in position: " + env.getPosition(cellToMove5).toString());
        env.removeNode(cellToMove5);
        env.removeNode(c6);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode6() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove6 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove6, originalPos);
        final Euclidean2DPosition p7 = new Euclidean2DPosition(-40, 0);
        final CellWithCircularArea<Euclidean2DPosition> c7 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c7, p7);
        env.moveNode(cellToMove6, POSITION_TO_MOVE6);
        assertEquals(env.getPosition(cellToMove6), EXPECTED_POS6, "cellToMove6 is in position: " + env.getPosition(cellToMove6).toString());
        env.removeNode(cellToMove6);
        env.removeNode(c7);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode7() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove7 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove7, originalPos);
        final Euclidean2DPosition p8 = new Euclidean2DPosition(40, 40);
        final CellWithCircularArea<Euclidean2DPosition> c8 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c8, p8);
        env.moveNode(cellToMove7, POSITION_TO_MOVE7);
        assertTrueJUnit4("cellToMove7 is in position: " + env.getPosition(cellToMove7).toString(),
                env.getPosition(cellToMove7).equals(EXPECTED_POS7));
        env.removeNode(cellToMove7);
        env.removeNode(c8);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode8() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove8 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove8, originalPos);
        final Euclidean2DPosition p9 = new Euclidean2DPosition(-40, -40);
        final CellWithCircularArea<Euclidean2DPosition> c9 = new CellNodeImpl<>(env, LITTLE_CELL_DIAMETER);
        env.addNode(c9, p9);
        env.moveNode(cellToMove8, POSITION_TO_MOVE8);
        assertTrueJUnit4("cellToMove8 is in position: " + env.getPosition(cellToMove8).toString(),
                env.getPosition(cellToMove8).equals(EXPECTED_POS8));
        env.removeNode(cellToMove8);
        env.removeNode(c9);
    }

    /*
     *  Here begins a section of the test class where the interactions between cell of different diameters are tested
     */

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    public void testAddDifferentDiam1() {
        env.addNode(ng1, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(10, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(0, 10);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(-10, -10);
        env.addNode(np1, p1);
        env.addNode(np2, p2);
        env.addNode(np3, p3);
        assertNullJUnit4("np1 not in pos null; it's in pos " + env.getPosition(np1),
                env.getPosition(np1));
        assertNullJUnit4("np2 not in pos null; it's in pos " + env.getPosition(np2),
                env.getPosition(np2));
        assertNullJUnit4("np3 not in pos null; it's in pos " + env.getPosition(np3),
                env.getPosition(np3));
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    public void testAddDifferentDiam2() {
        env.addNode(np1, originalPos);
        final Euclidean2DPosition p1 = new Euclidean2DPosition(10, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(20, 0);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(30, 0);
        env.addNode(ng1, p1);
        env.addNode(ng2, p2);
        env.addNode(ng3, p3);
        assertNullJUnit4("ng1 not in pos null; it's in pos " + env.getPosition(ng1),
                env.getPosition(ng1));
        assertTrueJUnit4(getFailureTestString("ng2", ng2, p2),
                env.getPosition(ng2).equals(p2));
        assertNullJUnit4(getFailureTestString("ng3", ng3, null),
                env.getPosition(ng3));
        env.removeNode(ng2);
        env.addNode(ng3, p3);
        assertTrueJUnit4(getFailureTestString("ng3", ng3, p3),
                env.getPosition(ng3).equals(p3));
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    public void testAddDifferentDiam3() {
        env.addNode(np1, originalPos);
        final Euclidean2DPosition p1 = new Euclidean2DPosition(20, 0);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(0, 15);
        final Euclidean2DPosition p3 = new Euclidean2DPosition(10, 10);
        env.addNode(ng1, p1);
        env.addNode(nm1, p2);
        env.addNode(nm2, p3);
        env.addNode(np2, p3);
        assertTrueJUnit4(getFailureTestString("ng1", ng1, p1),
                env.getPosition(ng1).equals(p1));
        assertTrueJUnit4(getFailureTestString("nm1", nm1, p2),
                env.getPosition(nm1).equals(p2));
        assertNullJUnit4(getFailureTestString("nm2", nm2, null),
                env.getPosition(nm2));
        assertNullJUnit4(getFailureTestString("np2", np2, null),
                env.getPosition(np2));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam1() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove1 = np1;
        env.addNode(cellToMove1, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove1, pd);
        assertTrueJUnit4("cellToMove1 is in position: " + env.getPosition(cellToMove1),
                env.getPosition(cellToMove1).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam2() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);
        final CellWithCircularArea<Euclidean2DPosition> cellToMove2 = np2;
        env.addNode(cellToMove2, originalPos);
        final CellWithCircularArea<Euclidean2DPosition> bce = new CellNodeImpl<>(env, 50);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(25, 30);
        env.addNode(bce, p2);
        env.moveNode(cellToMove2, pd);
        assertTrueJUnit4("cellToMove2 is in position: " + env.getPosition(cellToMove2),
                env.getPosition(cellToMove2).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam3() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);
        final CellWithCircularArea<Euclidean2DPosition> cellToMove3 = np3;
        env.addNode(cellToMove3, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 0);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove3, pd);
        assertTrueJUnit4("cellToMove3 is in position: " + env.getPosition(cellToMove3),
                env.getPosition(cellToMove3).equals(EXPECTED_POS_DIFFDIAM3_1));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove3, pd);
        assertTrueJUnit4("cellToMove3 is in position: " + env.getPosition(cellToMove3),
                env.getPosition(cellToMove3).equals(EXPECTED_POS_DIFFDIAM3_2));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam4() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(-50, 0);
        final CellWithCircularArea<Euclidean2DPosition> cellToMove4 = np3;
        env.addNode(cellToMove4, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(-25, 0);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove4, pd);
        assertTrueJUnit4("cellToMove4 is in position: " + env.getPosition(cellToMove4),
                env.getPosition(cellToMove4).equals(EXPECTED_POS_DIFFDIAM4_1));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove4, pd);
        assertTrueJUnit4("cellToMove4 is in position: " + env.getPosition(cellToMove4),
                env.getPosition(cellToMove4).equals(EXPECTED_POS_DIFFDIAM4_2));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam5() {
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 50);
        final CellWithCircularArea<Euclidean2DPosition> cellToMove5 = np3;
        env.addNode(cellToMove5, originalPos);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 25);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove5, pd);
        assertNotEquals(env.getPosition(cellToMove5), pd, "cellToMove5 is in position: " + env.getPosition(cellToMove5));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove5, pd);
        assertNotEquals(env.getPosition(cellToMove5), pd, "cellToMove5 is in position: " + env.getPosition(cellToMove5));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam6() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove6 = np1;
        env.addNode(cellToMove6, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        env.addNode(ng1, p1);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(-10, 0);
        env.addNode(np2, p2);
        env.moveNode(cellToMove6, pd);
        assertTrueJUnit4("cellToMove6 is in position: " + env.getPosition(cellToMove6), env.getPosition(cellToMove6).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam7() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove7 = np1;
        env.addNode(cellToMove7, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        env.addNode(ng1, p1);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(60, 5);
        env.addNode(np2, p2);
        env.moveNode(cellToMove7, pd);
        assertTrueJUnit4("cellToMove7 is in position: " + env.getPosition(cellToMove7),
                env.getPosition(cellToMove7).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam8() {
        final CellWithCircularArea<Euclidean2DPosition> cellToMove8 = np1;
        env.addNode(cellToMove8, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(50, 0);

        final Euclidean2DPosition p1 = new Euclidean2DPosition(25, 20);
        env.addNode(ng1, p1);
        final Euclidean2DPosition p2 = new Euclidean2DPosition(0, 10);
        env.addNode(np2, p2);
        env.moveNode(cellToMove8, pd);
        assertTrueJUnit4("cellToMove8 is in position: " + env.getPosition(cellToMove8),
                env.getPosition(cellToMove8).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others and if moving two times the cell the neighborhood is
     * correctly updated.
     */
    @Test
    public void testMoveInTwoSteps1() {
        final CellWithCircularArea<Euclidean2DPosition> c1 = np1;
        env.addNode(c1, originalPos);
        final Euclidean2DPosition pd1 = new Euclidean2DPosition(50, 0);
        final Euclidean2DPosition pd2 = new Euclidean2DPosition(100, 0);
        final CellWithCircularArea<Euclidean2DPosition> c2 = np2;
        env.addNode(c2, pd1);
        env.moveNode(c1, POSITION_TO_MOVE_TWOSTEP1);
        assertEquals(EXPECTED_POS_TWOSTEP1_1, env.getPosition(c1), "c1 is in pos : " + env.getPosition(c1));
        env.moveNode(c2, pd1);
        env.moveNodeToPosition(c1, pd2);
        assertEquals(EXPECTED_POS_TWOSTEP1_2, env.getPosition(c1), "c1 is in pos : " + env.getPosition(c1));

    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode9() {
        final CellWithCircularArea<Euclidean2DPosition> c1 = new CellNodeImpl<>(env, 1);
        final CellWithCircularArea<Euclidean2DPosition> c2 = new CellNodeImpl<>(env, 1);
        env.addNode(c2, NODE_POS9);
        env.addNode(c1, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(4.737000465393066, -5.0);
        env.moveNode(c1, pd);
        assertNotEquals(env.getPosition(c1), pd);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode10() {
        final CellWithCircularArea<Euclidean2DPosition> c1 = new CellNodeImpl<>(env, 1);
        final CellWithCircularArea<Euclidean2DPosition> c2 = new CellNodeImpl<>(env, 1);
        env.addNode(c2, NODE_POS10);
        env.addNode(c1, originalPos);
        final Euclidean2DPosition pd = new Euclidean2DPosition(3.122374292470004, -0.6490462479722794);
        env.moveNode(c1, pd);
        assertNotEquals(env.getPosition(c1), pd);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode11() {
        final double diameter = 1;
        final CellWithCircularArea<Euclidean2DPosition> c1 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c2 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c3 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c4 = new CellNodeImpl<>(env, diameter);
        env.addNode(c1, NODE_POS11_1);
        env.addNode(c2, NODE_POS11_2);
        env.addNode(c3, NODE_POS11_3);
        env.addNode(c4, NODE_POS11_4);
        final Euclidean2DPosition pd = new Euclidean2DPosition(5.0, -1.8431210525510544);
        env.moveNodeToPosition(c1, pd);
        assertTrueJUnit4("Should be empty but is : " + env.getNodesWithinRange(c1, c1.getDiameter()).stream()
                        .filter(n -> env.getDistanceBetweenNodes(c1, n) < diameter)
                        .map(n -> env.getPosition(n).toString())
                        .collect(Collectors.toList()),
                env.getNodesWithinRange(c1, c1.getDiameter() - DELTA).isEmpty());
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode12() {
        final double diameter = 1;
        final CellWithCircularArea<Euclidean2DPosition> c1 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c2 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c3 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c4 = new CellNodeImpl<>(env, diameter);
        env.addNode(c1, NODE_POS12_1);
        env.addNode(c2, NODE_POS12_2);
        env.addNode(c3, NODE_POS12_3);
        env.addNode(c4, NODE_POS12_4);
        final Euclidean2DPosition pd = new Euclidean2DPosition(5.3, -1.8431210525510544);
        env.moveNodeToPosition(c1, pd);
        assertTrueJUnit4("Should be empty but is : " + env.getNodesWithinRange(c1, c1.getDiameter()).stream()
                        .filter(n -> env.getDistanceBetweenNodes(c1, n) < diameter)
                        .map(n -> env.getPosition(n).toString())
                        .collect(Collectors.toList()),
                env.getNodesWithinRange(c1, c1.getDiameter()).isEmpty());
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode13() {
        final double diameter = 1;
        final CellWithCircularArea<Euclidean2DPosition> c1 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c2 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c3 = new CellNodeImpl<>(env, diameter);
        final CellWithCircularArea<Euclidean2DPosition> c4 = new CellNodeImpl<>(env, diameter);
        env.addNode(c1, NODE_POS13_1);
        env.addNode(c2, NODE_POS13_2);
        env.addNode(c3, NODE_POS13_3);
        env.addNode(c4, NODE_POS13_4);
        final Euclidean2DPosition pd = new Euclidean2DPosition(10, 10);
        env.moveNodeToPosition(c1, pd);
        env.moveNodeToPosition(c2, pd);
        env.moveNodeToPosition(c3, pd);
        env.moveNodeToPosition(c4, pd);
        assertTrueJUnit4("Should be empty but is : " + env.getNodesWithinRange(c1, c1.getDiameter()).stream()
                        .filter(n -> env.getDistanceBetweenNodes(c1, n) < diameter)
                        .map(n -> env.getPosition(n).toString())
                        .collect(Collectors.toList()),
                env.getNodesWithinRange(c1, c1.getDiameter()).isEmpty());
    }

    /**
     * Test in a simulation if there's no overlapping between cells.
     */
    @Test
    public void testNoOverlapInSimulation1() {
        testLoading("provaBCReaction.yml");
    }

    /**
     * Test in a simulation if there's no overlapping between cells.
     */
    @Test
    public void testNoOverlapInSimulation2() {
        testLoading("provaBCReaction2.yml");
    }

    private static void testLoading(final String resource) {
        final Map<String, Double> vars = Collections.emptyMap();
        final InputStream res = ResourceLoader.getResourceAsStream(resource);
        assertNotNull(res, "Missing test resource " + resource);
        final Environment<Double, Euclidean2DPosition> env = new YamlLoader(res).getWith(vars);
        final Simulation<Double, Euclidean2DPosition> sim = new Engine<>(env, 10000);
        sim.addOutputMonitor(new OutputMonitor<Double, Euclidean2DPosition>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void stepDone(final Environment<Double, Euclidean2DPosition> env, final Reaction<Double> r, final Time time, final long step) {
                assertTrue(thereIsOverlap(env), "Fail at time: " + time);
            }

            @Override
            public void initialized(final Environment<Double, Euclidean2DPosition> env) {
                assertTrue(thereIsOverlap(env));
            }

            @Override
            public void finished(final Environment<Double, Euclidean2DPosition> env, final Time time, final long step) {
                assertTrue(thereIsOverlap(env));
            }

            private Stream<CellWithCircularArea<Euclidean2DPosition>> getNodes() {
                return env.getNodes().stream()
                        .filter(n -> n instanceof CellWithCircularArea)
                        .map(n -> (CellWithCircularArea<Euclidean2DPosition>) n);
            }

            private boolean thereIsOverlap(final Environment<Double, Euclidean2DPosition> env) {
                getNodes().flatMap(n -> getNodes()
                        .filter(c -> !c.equals(n))
                        .filter(c -> env.getDistanceBetweenNodes(n, c) < n.getRadius() + c.getRadius() - DELTA)
                        .map(c -> new Pair<>(n, c)))
                        .findAny()
                        .ifPresent(e -> fail("Nodes " + e.getFirst().getId() + env.getPosition(e.getFirst()) + " and "
                            + e.getSecond().getId() + env.getPosition(e.getSecond()) + " are overlapping. "
                            + "Their distance is: " + env.getDistanceBetweenNodes(e.getFirst(), e.getSecond())
                            + " but should be greater than " + (e.getFirst().getRadius() + e.getSecond().getRadius())));
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
        return nodeName + " not in pos " + expected + "; it's in pos " + env.getPosition(n);
    }

    private static void assertTrueJUnit4(final String msg, final boolean res) {
        assertTrue(res, msg);
    }

    private static void assertNullJUnit4(final String msg, final Object res) {
        assertNull(res, msg);
    }

}
