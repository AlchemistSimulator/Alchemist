package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 *
 */
public class TestBioRect2DEnvironmentNoOverlap {

    private static final double MAX_PRECISION = 0.9999999999;
    private static final double BIG_CELL_DIAMETER = 30;
    private static final double MEDIUM_CELL_DIAMETER = 20;
    private static final double LITTLE_CELL_DIAMETER = 10;
    private static final Position POSITION_TO_MOVE1 = new Continuous2DEuclidean(80, 0);
    private static final Position EXPECTED_POS1 = new Continuous2DEuclidean(30, 0);
    private static final Position POSITION_TO_MOVE2 = POSITION_TO_MOVE1;
    private static final Position EXPECTED_POS2 = new Continuous2DEuclidean((4 * 10) - FastMath.sqrt(75), 0);
    private static final Position POSITION_TO_MOVE3 = new Continuous2DEuclidean(80, 0);
    private static final Position POSITION_TO_MOVE4 = POSITION_TO_MOVE3;
    private static final Position POSITION_TO_MOVE5 = POSITION_TO_MOVE3;
    private static final Position POSITION_TO_MOVE6 = new Continuous2DEuclidean(-80, 0);
    private static final Position EXPECTED_POS6 = new Continuous2DEuclidean(-30, 0);
    private static final Position POSITION_TO_MOVE7 = new Continuous2DEuclidean(80, 80);
    private static final Position EXPECTED_POS7 = new Continuous2DEuclidean(40 - (10 / FastMath.sqrt(2)), 40 - (10 / FastMath.sqrt(2)));
    private static final Position POSITION_TO_MOVE8 = new Continuous2DEuclidean(-80, -80);
    private static final Position EXPECTED_POS8 = new Continuous2DEuclidean(-40 + (10 / FastMath.sqrt(2)), -40 + (10 / FastMath.sqrt(2)));
    private static final Position EXPECTED_POS_DIFFDIAM3_1 = new Continuous2DEuclidean(5, 0);
    private static final Position EXPECTED_POS_DIFFDIAM3_2 = new Continuous2DEuclidean(10, 0);
    private static final Position EXPECTED_POS_DIFFDIAM4_1 = new Continuous2DEuclidean(-5, 0);
    private static final Position EXPECTED_POS_DIFFDIAM4_2 = new Continuous2DEuclidean(-10, 0);
    private static final Position POSITION_TO_MOVE_TWOSTEP1 = new Continuous2DEuclidean(50, 0);
    private static final Position EXPECTED_POS_TWOSTEP1_1 = new Continuous2DEuclidean(40, 0);
    private static final Position EXPECTED_POS_TWOSTEP1_2 = new Continuous2DEuclidean(90, 0);
    private static final Position NODE_POS9 = new Continuous2DEuclidean(4, -5);
    private static final Position NODE_POS10 = new Continuous2DEuclidean(2.813191105618545, 0.3019562530593296);
    private static final Position NODE_POS11_1 = new Continuous2DEuclidean(1.2915251125665559, 1.7945837966921097);
    private static final Position NODE_POS11_2 = new Continuous2DEuclidean(4.773603784764428, 0.23619996027968504);
    private static final Position NODE_POS11_3 = new Continuous2DEuclidean(0.16085716189097174, 0.04968203900319437);
    private static final Position NODE_POS11_4 = new Continuous2DEuclidean(3.122374292470004, -0.6490462479722794);
    private static final Position NODE_POS12_1 = new Continuous2DEuclidean(1.2915251125665559, 1.7945837966921097);
    private static final Position NODE_POS12_2 = new Continuous2DEuclidean(4.773603784764428, 0.23619996027968504);
    private static final Position NODE_POS12_3 = new Continuous2DEuclidean(0.16085716189097174, 0.04968203900319437);
    private static final Position NODE_POS12_4 = new Continuous2DEuclidean(3.122374292470004, -0.6490462479722794);
    private static final Position NODE_POS13_1 = new Continuous2DEuclidean(5, 5);
    private static final Position NODE_POS13_2 = new Continuous2DEuclidean(-5, 5);
    private static final Position NODE_POS13_3 = new Continuous2DEuclidean(-5, -5);
    private static final Position NODE_POS13_4 = new Continuous2DEuclidean(5, -5);
    private final Position originalPos = new Continuous2DEuclidean(0, 0);
    private CellWithCircularArea ng1; 
    private CellWithCircularArea ng2; 
    private CellWithCircularArea ng3;
    private CellWithCircularArea nm1;
    private CellWithCircularArea nm2;
    private CellWithCircularArea np1;
    private CellWithCircularArea np2;
    private CellWithCircularArea np3;

    private Environment<Double> env;

    /**
     * 
     */
    @Before
    public void setUp() {
        env = new BioRect2DEnvironmentNoOverlap();
        env.setLinkingRule(new NoLinks<>());
        ng1 = new CellNodeImpl(env, BIG_CELL_DIAMETER);
        ng2 = new CellNodeImpl(env, BIG_CELL_DIAMETER);
        ng3 = new CellNodeImpl(env, BIG_CELL_DIAMETER);
        nm1 = new CellNodeImpl(env, MEDIUM_CELL_DIAMETER);
        nm2 = new CellNodeImpl(env, MEDIUM_CELL_DIAMETER);
        np1 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        np2 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        np3 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    public void testAddNode() {
        final CellWithCircularArea n1 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n2 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n3 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n4 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n5 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n6 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n7 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n8 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n9 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n10 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea n11 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);

        final Position p1 = originalPos;
        final Position p2 = new Continuous2DEuclidean(10, 0);
        env.addNode(n1, p1);
        env.addNode(n2, p2);

        final Position p3 = new Continuous2DEuclidean(0, 20); // this should be added
        env.addNode(n3, p3);
        assertTrue(getFailureTestString("n3", n3, p3), 
                env.getPosition(n3).equals(p3)); 
        env.removeNode(n3);
        final Position p4 = new Continuous2DEuclidean(0, 10); // this should be added
        env.addNode(n4, p4);
        assertTrue(getFailureTestString("n4", n4, p4),
                env.getPosition(n4).equals(p4));
        env.removeNode(n4);
        final Position p5 = new Continuous2DEuclidean(0, 5); // this should not be added
        env.addNode(n5, p5);
        assertTrue(getFailureTestString("n5", n5, null),
                env.getPosition(n5) == (null));
        final Position p6 = new Continuous2DEuclidean(5, 0); // this should not be added
        env.addNode(n6, p6);
        assertTrue(getFailureTestString("n6", n6, null),
                env.getPosition(n6) == (null));
        final Position p7 = new Continuous2DEuclidean(0, 0); // this should not be added
        env.addNode(n7, p7);
        assertNull(getFailureTestString("n7", n7, null),
                env.getPosition(n7));
        final Position p8 = new Continuous2DEuclidean(10, 0); // this should not be added
        env.addNode(n8, p8);
        assertNull(getFailureTestString("n8", n8, null),
                env.getPosition(n8));
        final Position p9 = new Continuous2DEuclidean(20, 0); // this should be added
        env.addNode(n9, p9);
        assertTrue(getFailureTestString("n9", n9, p9),
                env.getPosition(n9).equals(p9));
        env.removeNode(n9);
        final Position p10 = new Continuous2DEuclidean(2.5, 2.5); // this should not be added
        env.addNode(n10, p10);
        assertTrue(getFailureTestString("n10", n10, null),
                env.getPosition(n10) == (null));
        final Position p11 =  new Continuous2DEuclidean(7.5, -2.5); // this should not be added
        env.addNode(n11, p11);
        assertTrue(getFailureTestString("n11", n11, null),
                env.getPosition(n11) == (null));

        env.removeNode(n1);
        env.removeNode(n2);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode1() {
        final CellWithCircularArea cellToMove1 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove1, originalPos);
        final Position p1 = new Continuous2DEuclidean(40, 0);
        final CellWithCircularArea c1 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c1, p1);
        env.moveNode(cellToMove1, POSITION_TO_MOVE1);
        assertTrue("cellToMove1 is in position: " + env.getPosition(cellToMove1),
                env.getPosition(cellToMove1).equals(EXPECTED_POS1));
        env.removeNode(cellToMove1);
        env.removeNode(c1);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode2() {
        final CellWithCircularArea cellToMove2 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove2, originalPos);
        final Position p2 = new Continuous2DEuclidean(40, 5);
        final Position p3 = new Continuous2DEuclidean(40, -5);
        final CellWithCircularArea c2 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        final CellWithCircularArea c3 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c2, p2);
        env.addNode(c3, p3);
        env.moveNode(cellToMove2, POSITION_TO_MOVE2);
        assertTrue("cellToMove2 is in position: " + env.getPosition(cellToMove2).toString(),
                env.getPosition(cellToMove2).equals(EXPECTED_POS2));
        env.removeNode(cellToMove2);
        env.removeNode(c2);
        env.removeNode(c3);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode3() {
        final CellWithCircularArea cellToMove3 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove3, originalPos);
        final Position p4 = new Continuous2DEuclidean(10, 0);
        final CellWithCircularArea c4 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c4, p4);
        env.moveNode(cellToMove3, POSITION_TO_MOVE3);
        assertTrue("cellToMove3 is in position: " + env.getPosition(cellToMove3).toString(),
                env.getPosition(cellToMove3).equals(originalPos));
        env.removeNode(cellToMove3);
        env.removeNode(c4);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode4() {
        final CellWithCircularArea cellToMove4 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove4, originalPos);
        final Position p5 = new Continuous2DEuclidean(0.2, FastMath.sqrt(FastMath.pow(cellToMove4.getDiameter(), 2) - FastMath.pow(0.2, 2)));
        final CellWithCircularArea c5 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c5, p5);
        env.moveNode(cellToMove4, POSITION_TO_MOVE4);
        final Position expectedPos = POSITION_TO_MOVE4;
        assertFalse("cellToMove4 is in position: " + env.getPosition(cellToMove4).toString(),
                env.getPosition(cellToMove4).equals(expectedPos));
        env.removeNode(cellToMove4);
        env.removeNode(c5);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode5() {
        final CellWithCircularArea cellToMove5 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove5, originalPos);
        final Position p6 = new Continuous2DEuclidean(20, 10);
        final CellWithCircularArea c6 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c6, p6);
        env.moveNode(cellToMove5, POSITION_TO_MOVE5);
        final Position expectedPos = POSITION_TO_MOVE5;
        assertTrue("cellToMove5 is in position: " + env.getPosition(cellToMove5).toString(),
                env.getPosition(cellToMove5).equals(expectedPos));
        env.removeNode(cellToMove5);
        env.removeNode(c6);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode6() {
        final CellWithCircularArea cellToMove6 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove6, originalPos);
        final Position p7 = new Continuous2DEuclidean(-40, 0);
        final CellWithCircularArea c7 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c7, p7);
        env.moveNode(cellToMove6, POSITION_TO_MOVE6);
        assertTrue("cellToMove6 is in position: " + env.getPosition(cellToMove6).toString(),
                env.getPosition(cellToMove6).equals(EXPECTED_POS6));
        env.removeNode(cellToMove6);
        env.removeNode(c7);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode7() {
        final CellWithCircularArea cellToMove7 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove7, originalPos);
        final Position p8 = new Continuous2DEuclidean(40, 40);
        final CellWithCircularArea c8 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c8, p8);
        env.moveNode(cellToMove7, POSITION_TO_MOVE7);
        assertTrue("cellToMove7 is in position: " + env.getPosition(cellToMove7).toString(),
                env.getPosition(cellToMove7).equals(EXPECTED_POS7));
        env.removeNode(cellToMove7);
        env.removeNode(c8);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode8() {
        final CellWithCircularArea cellToMove8 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(cellToMove8, originalPos);
        final Position p9 = new Continuous2DEuclidean(-40, -40);
        final CellWithCircularArea c9 = new CellNodeImpl(env, LITTLE_CELL_DIAMETER);
        env.addNode(c9, p9);
        env.moveNode(cellToMove8, POSITION_TO_MOVE8);
        assertTrue("cellToMove8 is in position: " + env.getPosition(cellToMove8).toString(),
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

        final Position p1 = new Continuous2DEuclidean(10, 0);
        final Position p2 = new Continuous2DEuclidean(0, 10);
        final Position p3 = new Continuous2DEuclidean(-10, -10);
        env.addNode(np1, p1);
        env.addNode(np2, p2);
        env.addNode(np3, p3);
        assertNull("np1 not in pos null; it's in pos " + env.getPosition(np1),
                env.getPosition(np1));
        assertNull("np2 not in pos null; it's in pos " + env.getPosition(np2),
                env.getPosition(np2));
        assertNull("np3 not in pos null; it's in pos " + env.getPosition(np3),
                env.getPosition(np3));
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    public void testAddDifferentDiam2() {
        env.addNode(np1, originalPos);
        final Position p1 = new Continuous2DEuclidean(10, 0);
        final Position p2 = new Continuous2DEuclidean(20, 0);
        final Position p3 = new Continuous2DEuclidean(30, 0);
        env.addNode(ng1, p1);
        env.addNode(ng2, p2);
        env.addNode(ng3, p3);
        assertNull("ng1 not in pos null; it's in pos " + env.getPosition(ng1),
                env.getPosition(ng1));
        assertTrue(getFailureTestString("ng2", ng2, p2),
                env.getPosition(ng2).equals(p2));
        assertNull(getFailureTestString("ng3", ng3, null),
                env.getPosition(ng3));
        env.removeNode(ng2);
        env.addNode(ng3, p3);
        assertTrue(getFailureTestString("ng3", ng3, p3),
                env.getPosition(ng3).equals(p3));
    }

    /**
     * Testing if the node are correctly added, given their dimension in space.
     */
    @Test
    public void testAddDifferentDiam3() {
        env.addNode(np1, originalPos);
        final Position p1 = new Continuous2DEuclidean(20, 0);
        final Position p2 = new Continuous2DEuclidean(0, 15);
        final Position p3 = new Continuous2DEuclidean(10, 10);
        env.addNode(ng1, p1);
        env.addNode(nm1, p2);
        env.addNode(nm2, p3);
        env.addNode(np2, p3);
        assertTrue(getFailureTestString("ng1", ng1, p1),
                env.getPosition(ng1).equals(p1));
        assertTrue(getFailureTestString("nm1", nm1, p2),
                env.getPosition(nm1).equals(p2));
        assertNull(getFailureTestString("nm2", nm2, null),
                env.getPosition(nm2));
        assertNull(getFailureTestString("np2", np2, null),
                env.getPosition(np2));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam1() {
        final CellWithCircularArea cellToMove1 = np1;
        env.addNode(cellToMove1, originalPos);
        final Position pd = new Continuous2DEuclidean(50, 0);

        final Position p1 = new Continuous2DEuclidean(25, 20);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove1, pd);
        assertTrue("cellToMove1 is in position: " + env.getPosition(cellToMove1),
                env.getPosition(cellToMove1).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam2() {
        final Position pd = new Continuous2DEuclidean(50, 0);
        final CellWithCircularArea cellToMove2 = np2;
        env.addNode(cellToMove2, originalPos);
        final CellWithCircularArea bce = new CellNodeImpl(env, 50);
        final Position p2 = new Continuous2DEuclidean(25, 30);
        env.addNode(bce, p2);
        env.moveNode(cellToMove2, pd);
        assertTrue("cellToMove2 is in position: " + env.getPosition(cellToMove2),
                env.getPosition(cellToMove2).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam3() {
        final Position pd = new Continuous2DEuclidean(50, 0);
        final CellWithCircularArea cellToMove3 = np3;
        env.addNode(cellToMove3, originalPos);

        final Position p1 = new Continuous2DEuclidean(25, 0);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove3, pd);
        assertTrue("cellToMove3 is in position: " + env.getPosition(cellToMove3),
                env.getPosition(cellToMove3).equals(EXPECTED_POS_DIFFDIAM3_1));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove3, pd);
        assertTrue("cellToMove3 is in position: " + env.getPosition(cellToMove3),
                env.getPosition(cellToMove3).equals(EXPECTED_POS_DIFFDIAM3_2));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam4() {
        final Position pd = new Continuous2DEuclidean(-50, 0);
        final CellWithCircularArea cellToMove4 = np3;
        env.addNode(cellToMove4, originalPos);

        final Position p1 = new Continuous2DEuclidean(-25, 0);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove4, pd);
        assertTrue("cellToMove4 is in position: " + env.getPosition(cellToMove4),
                env.getPosition(cellToMove4).equals(EXPECTED_POS_DIFFDIAM4_1));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove4, pd);
        assertTrue("cellToMove4 is in position: " + env.getPosition(cellToMove4),
                env.getPosition(cellToMove4).equals(EXPECTED_POS_DIFFDIAM4_2));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam5() {
        final Position pd = new Continuous2DEuclidean(50, 50);
        final CellWithCircularArea cellToMove5 = np3;
        env.addNode(cellToMove5, originalPos);

        final Position p1 = new Continuous2DEuclidean(25, 25);
        env.addNode(ng1, p1);
        env.moveNode(cellToMove5, pd);
        assertFalse("cellToMove5 is in position: " + env.getPosition(cellToMove5),
                env.getPosition(cellToMove5).equals(pd));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove5, pd);
        assertFalse("cellToMove5 is in position: " + env.getPosition(cellToMove5),
                env.getPosition(cellToMove5).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam6() {
        final CellWithCircularArea cellToMove6 = np1;
        env.addNode(cellToMove6, originalPos);
        final Position pd = new Continuous2DEuclidean(50, 0);

        final Position p1 = new Continuous2DEuclidean(25, 20);
        env.addNode(ng1, p1); 
        final Position p2 = new Continuous2DEuclidean(-10, 0);
        env.addNode(np2, p2); 
        env.moveNode(cellToMove6, pd);
        assertTrue("cellToMove6 is in position: " + env.getPosition(cellToMove6),
                env.getPosition(cellToMove6).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam7() {
        final CellWithCircularArea cellToMove7 = np1;
        env.addNode(cellToMove7, originalPos);
        final Position pd = new Continuous2DEuclidean(50, 0);

        final Position p1 = new Continuous2DEuclidean(25, 20);
        env.addNode(ng1, p1); 
        final Position p2 = new Continuous2DEuclidean(60, 5);
        env.addNode(np2, p2); 
        env.moveNode(cellToMove7, pd);
        assertTrue("cellToMove7 is in position: " + env.getPosition(cellToMove7),
                env.getPosition(cellToMove7).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveDifferentDiam8() {
        final CellWithCircularArea cellToMove8 = np1;
        env.addNode(cellToMove8, originalPos);
        final Position pd = new Continuous2DEuclidean(50, 0);

        final Position p1 = new Continuous2DEuclidean(25, 20);
        env.addNode(ng1, p1); 
        final Position p2 = new Continuous2DEuclidean(0, 10);
        env.addNode(np2, p2); 
        env.moveNode(cellToMove8, pd);
        assertTrue("cellToMove8 is in position: " + env.getPosition(cellToMove8),
                env.getPosition(cellToMove8).equals(pd));
    }

    /**
     * Testing if node moves respecting dimension of all the others and if moving two times the cell the neighborhood is corectly updated.
     */
    @Test
    public void testMoveInTwoSteps1() {
        final CellWithCircularArea c1 = np1;
        env.addNode(c1, originalPos);
        final Position pd1 = new Continuous2DEuclidean(50, 0);
        final Position pd2 = new Continuous2DEuclidean(100, 0);
        final CellWithCircularArea c2 = np2;
        env.addNode(c2, pd1);
        env.moveNode(c1, POSITION_TO_MOVE_TWOSTEP1);
        assertEquals("c1 is in pos : " + env.getPosition(c1), EXPECTED_POS_TWOSTEP1_1, env.getPosition(c1));
        env.moveNode(c2, pd1);
        env.moveNodeToPosition(c1, pd2);
        assertEquals("c1 is in pos : " + env.getPosition(c1), EXPECTED_POS_TWOSTEP1_2, env.getPosition(c1));

    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode9() {
        final CellWithCircularArea c1 = new CellNodeImpl(env, 1);
        final CellWithCircularArea c2 = new CellNodeImpl(env, 1);
        env.addNode(c2, NODE_POS9);
        env.addNode(c1, originalPos);
        final Position pd = new Continuous2DEuclidean(4.737000465393066, -5.0);
        env.moveNode(c1, pd);
        assertNotEquals(env.getPosition(c1), pd);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode10() {
        final CellWithCircularArea c1 = new CellNodeImpl(env, 1);
        final CellWithCircularArea c2 = new CellNodeImpl(env, 1);
        env.addNode(c2, NODE_POS10);
        env.addNode(c1, originalPos);
        final Position pd = new Continuous2DEuclidean(3.122374292470004, -0.6490462479722794);
        env.moveNode(c1, pd);
        assertNotEquals(env.getPosition(c1), pd);
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode11() {
        final double diameter = 1;
        final CellWithCircularArea c1 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c2 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c3 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c4 = new CellNodeImpl(env, diameter);
        env.addNode(c1, NODE_POS11_1);
        env.addNode(c2, NODE_POS11_2);
        env.addNode(c3, NODE_POS11_3);
        env.addNode(c4, NODE_POS11_4); 
        final Position pd = new Continuous2DEuclidean(5.0, -1.8431210525510544);
        env.moveNodeToPosition(c1, pd);
        assertTrue("Should be empty but is : " + env.getNodesWithinRange(c1, c1.getDiameter()).stream()
                .filter(n -> env.getDistanceBetweenNodes(c1, n) < diameter)
                .map(n -> env.getPosition(n).toString())
                .collect(Collectors.toList()),
                env.getNodesWithinRange(c1, c1.getDiameter() * MAX_PRECISION).isEmpty());
    }

    /**
     * Testing if node moves respecting dimension of all the others.
     */
    @Test
    public void testMoveNode12() {
        final double diameter = 1;
        final CellWithCircularArea c1 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c2 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c3 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c4 = new CellNodeImpl(env, diameter);
        env.addNode(c1, NODE_POS12_1);
        env.addNode(c2, NODE_POS12_2);
        env.addNode(c3, NODE_POS12_3);
        env.addNode(c4, NODE_POS12_4); 
        final Position pd = new Continuous2DEuclidean(5.3, -1.8431210525510544);
        env.moveNodeToPosition(c1, pd);
        assertTrue("Should be empty but is : " + env.getNodesWithinRange(c1, c1.getDiameter()).stream()
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
        final CellWithCircularArea c1 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c2 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c3 = new CellNodeImpl(env, diameter);
        final CellWithCircularArea c4 = new CellNodeImpl(env, diameter);
        env.addNode(c1, NODE_POS13_1);
        env.addNode(c2, NODE_POS13_2);
        env.addNode(c3, NODE_POS13_3);
        env.addNode(c4, NODE_POS13_4); 
        final Position pd = new Continuous2DEuclidean(10, 10);
        env.moveNodeToPosition(c1, pd);
        env.moveNodeToPosition(c2, pd);
        env.moveNodeToPosition(c3, pd);
        env.moveNodeToPosition(c4, pd);
        assertTrue("Should be empty but is : " + env.getNodesWithinRange(c1, c1.getDiameter()).stream()
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
        testNoVar("/provaBCReaction.yml");
    }

    /**
     * Test in a simulation if there's no overlapping between cells.
     */
    @Test
    public void testNoOverlapInSimulation2() {
        testNoVar2("/provaBCReaction2.yml");
    }

    private static void testNoVar(final String resource) {
        testLoading(resource, Collections.emptyMap());
    }

    private static void testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = YamlLoader.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<Double> env = new YamlLoader(res).getWith(vars);
        final Simulation<Double> sim = new Engine<>(env, 10000);
        sim.play();
        sim.addOutputMonitor(new OutputMonitor<Double>() {

            /**
             * 
             */
            private static final long serialVersionUID = -6746841308070417583L;

            @Override
            public void stepDone(final Environment<Double> env, final Reaction<Double> r, final Time time, final long step) {
                assertTrue("Fail at time: " + time, thereIsOverlap(env));
            }

            @Override
            public void initialized(final Environment<Double> env) {
                assertTrue(thereIsOverlap(env));
            }

            @Override
            public void finished(final Environment<Double> env, final Time time, final long step) {
                assertTrue(thereIsOverlap(env));
            }

            private boolean thereIsOverlap(final Environment<Double> env) {
                final boolean posResult =  env.getNodes().stream()
                        .filter(n -> n instanceof CellWithCircularArea)
                        .map(n -> env.getNodesWithinRange(n, ((CellWithCircularArea) n).getDiameter()).stream()
                                .filter(c -> env.getDistanceBetweenNodes(c, n) < (((CellWithCircularArea) n).getDiameter() * MAX_PRECISION))
                                .collect(Collectors.toList()).isEmpty())
                        .allMatch(b -> b);
                if (posResult) {
                    return posResult;
                } else {
                    /* debug 
                    env.getNodes().stream()
                    .filter(n -> n instanceof CellWithCircularArea)
                    .forEach(n -> {
                        final List<Node<Double>> listOverlapping = env.getNodesWithinRange(n, (((CellWithCircularArea) n).getDiameter())).stream()
                                .filter(c -> env.getDistanceBetweenNodes(c, n) < ((CellWithCircularArea) n).getDiameter())
                                .collect(Collectors.toList());
                        if (!listOverlapping.isEmpty()) {
                            System.out.println("nodes: ");
                            System.out.println("center : " + env.getPosition(n));
                            listOverlapping.forEach(c -> System.out.println("In range : " + env.getPosition(c)));
                            listOverlapping.forEach(c -> System.out.println("distance : " + env.getPosition(c).getDistanceTo(env.getPosition(n))));
                        }
                    });
                    */
                    return posResult;
                }
            }
        });
        sim.run();
    }

    private static void testNoVar2(final String resource) {
        testLoading2(resource, Collections.emptyMap());
    }

    private static void testLoading2(final String resource, final Map<String, Double> vars) {
        final InputStream res = YamlLoader.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<Double> env = new YamlLoader(res).getWith(vars);
        final Simulation<Double> sim = new Engine<>(env, 10000);
        sim.play();
        sim.addOutputMonitor(new OutputMonitor<Double>() {

            /**
             * 
             */
            private static final long serialVersionUID = -6746841308070417583L;

            @Override
            public void stepDone(final Environment<Double> env, final Reaction<Double> r, final Time time, final long step) {
                assertTrue("Fail at time: " + time, thereIsOverlap(env));
            }

            @Override
            public void initialized(final Environment<Double> env) {
                assertTrue(thereIsOverlap(env));
            }

            @Override
            public void finished(final Environment<Double> env, final Time time, final long step) {
                assertTrue(thereIsOverlap(env));
            }

            private boolean thereIsOverlap(final Environment<Double> env) {
                final boolean posResult =  env.getNodes().stream()
                        .filter(n -> n instanceof CellWithCircularArea)
                        .map(n -> env.getNodesWithinRange(n, 4).stream()
                                .filter(c -> env.getDistanceBetweenNodes(c, n) < ((((CellWithCircularArea) n).getRadius() + ((CellWithCircularArea) c).getRadius()) * MAX_PRECISION))
                                .collect(Collectors.toList()).isEmpty())
                        .allMatch(b -> b);
                return posResult;
                /* debug
                if (posResult) {
                    return posResult;
                } else {
                    env.getNodes().stream()
                    .filter(n -> n instanceof CellWithCircularArea)
                    .forEach(n -> {
                        final List<Node<Double>> listOverlapping = env.getNodesWithinRange(n, (((CellWithCircularArea) n).getDiameter())).stream()
                                .filter(c -> env.getDistanceBetweenNodes(c, n) < ((((CellWithCircularArea) n).getRadius() + ((CellWithCircularArea) c).getRadius())))
                                .collect(Collectors.toList());
                        if (!listOverlapping.isEmpty()) {
                            System.out.println("nodes: ");
                            System.out.println(n + " center : " + env.getPosition(n));
                            listOverlapping.forEach(c -> System.out.println(c + " In range : " + env.getPosition(c)));
                            listOverlapping.forEach(c -> System.out.println("distance : " + env.getPosition(c).getDistanceTo(env.getPosition(n))));
                        }
                    });
                    return posResult;
                }
                */
            }
        });
        sim.run();
    }

    private String getFailureTestString(final String nodeName, final Node<Double> n, final Position expected) {
        return nodeName + " not in pos " + expected + "; it's in pos " + env.getPosition(n);
    }

}
