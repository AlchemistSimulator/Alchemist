package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 *
 */
public class TestBioRect2DEnviromentNoOverlap {

    private static final double STANDARD_DIAMETER = 10;
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
        ng1 = new CellNodeImpl(env, 3 * STANDARD_DIAMETER);
        ng2 = new CellNodeImpl(env, 3 * STANDARD_DIAMETER);
        ng3 = new CellNodeImpl(env, 3 * STANDARD_DIAMETER);
        nm1 = new CellNodeImpl(env, 2 * STANDARD_DIAMETER);
        nm2 = new CellNodeImpl(env, 2 * STANDARD_DIAMETER);
        np1 = new CellNodeImpl(env, STANDARD_DIAMETER);
        np2 = new CellNodeImpl(env, STANDARD_DIAMETER);
        np3 = new CellNodeImpl(env, STANDARD_DIAMETER);
    }

    /**
     * 
     */
    @Test
    public void testAddNode() {
        final CellWithCircularArea n1 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n2 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n3 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n4 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n5 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n6 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n7 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n8 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n9 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n10 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea n11 = new CellNodeImpl(env, STANDARD_DIAMETER);

        final Position p1 = originalPos;
        final Position p2 = new Continuous2DEuclidean(STANDARD_DIAMETER, 0);
        env.addNode(n1, p1);
        env.addNode(n2, p2);

        final Position p3 = new Continuous2DEuclidean(0, 2 * STANDARD_DIAMETER); //ok
        env.addNode(n3, p3);
        assertTrue("n3 not in pos " + p3.toString() + "; it's in pos " + env.getPosition(n3),
                env.getPosition(n3).equals(p3));
        env.removeNode(n3);
        final Position p4 = new Continuous2DEuclidean(0, STANDARD_DIAMETER); //ok
        env.addNode(n4, p4);
        assertTrue("n4 not in pos " + p4.toString() + "; it's in pos " + env.getPosition(n4),
                env.getPosition(n4).equals(p4));
        env.removeNode(n4);
        final Position p5 = new Continuous2DEuclidean(0, STANDARD_DIAMETER / 2); // fail
        env.addNode(n5, p5);
        assertTrue("n5 not in pos " + null + "; it's in pos " + env.getPosition(n5),
                env.getPosition(n5) == (null));
        final Position p6 = new Continuous2DEuclidean(STANDARD_DIAMETER / 2, 0); // fail
        env.addNode(n6, p6);
        assertTrue("n6 not in pos " + null + "; it's in pos " + env.getPosition(n6),
                env.getPosition(n6) == (null));
        final Position p7 = new Continuous2DEuclidean(0, 0); //fail
        env.addNode(n7, p7);
        assertNull("n7 not in pos " + null + "; it's in pos " + env.getPosition(n7),
                env.getPosition(n7));
        final Position p8 = new Continuous2DEuclidean(STANDARD_DIAMETER, 0); //fail
        env.addNode(n8, p8);
        assertNull("n8 not in pos " + null + "; it's in pos " + env.getPosition(n8),
                env.getPosition(n8));
        final Position p9 = new Continuous2DEuclidean(2 * STANDARD_DIAMETER, 0); //ok
        env.addNode(n9, p9);
        assertTrue("n9 not in pos " + p9.toString() + "; it's in pos " + env.getPosition(n9),
                env.getPosition(n9).equals(p9));
        env.removeNode(n9);
        final Position p10 = new Continuous2DEuclidean(STANDARD_DIAMETER / 4, STANDARD_DIAMETER / 4); //fail
        env.addNode(n10, p10);
        assertTrue("n10 not in pos " + null + "; it's in pos " + env.getPosition(n10),
                env.getPosition(n10) == (null));
        final Position p11 =  new Continuous2DEuclidean((3 / 4) * STANDARD_DIAMETER, -(STANDARD_DIAMETER / 4)); //fail
        env.addNode(n11, p11);
        assertTrue("n11 not in pos " + null + "; it's in pos " + env.getPosition(n11),
                env.getPosition(n11) == (null));

        env.removeNode(n1);
        env.removeNode(n2);
    }

    /**
     * 
     */
    @Test
    public void testMoveNode1() {
        // test1
        final CellWithCircularArea cellToMove1 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove1, originalPos);
        final Position p1 = new Continuous2DEuclidean(40, 0);
        final CellWithCircularArea c1 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c1, p1);
        env.moveNode(cellToMove1, new Continuous2DEuclidean(8 * STANDARD_DIAMETER, 0));
        assertTrue("cellToMove1 is in position: " + env.getPosition(cellToMove1).toString(),
                env.getPosition(cellToMove1).equals(new Continuous2DEuclidean(3 * STANDARD_DIAMETER, 0)));
        env.removeNode(cellToMove1);
        env.removeNode(c1);
    }

    /**
     * 
     */
    @Test
    public void testMoveNode2() {
        // test2 
        final CellWithCircularArea cellToMove2 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove2, originalPos);
        final Position p2 = new Continuous2DEuclidean(40, 5);
        final Position p3 = new Continuous2DEuclidean(40, -5);
        final CellWithCircularArea c2 = new CellNodeImpl(env, STANDARD_DIAMETER);
        final CellWithCircularArea c3 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c2, p2);
        env.addNode(c3, p3);
        env.moveNode(cellToMove2, new Continuous2DEuclidean(8 * STANDARD_DIAMETER, 0));
        assertTrue("cellToMove2 is in position: " + env.getPosition(cellToMove2).toString(),
                env.getPosition(cellToMove2).equals(new Continuous2DEuclidean((4 * STANDARD_DIAMETER) - FastMath.sqrt(75), 0)));
        env.removeNode(cellToMove2);
        env.removeNode(c2);
        env.removeNode(c3);
    }

    /**
     * 
     */
    @Test
    public void testMoveNode3() {
        // test3
        final CellWithCircularArea cellToMove3 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove3, originalPos);
        final Position p4 = new Continuous2DEuclidean(STANDARD_DIAMETER, 0);
        final CellWithCircularArea c4 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c4, p4);
        env.moveNode(cellToMove3, new Continuous2DEuclidean(80, 0));
        assertTrue("cellToMove3 is in position: " + env.getPosition(cellToMove3).toString(),
                env.getPosition(cellToMove3).equals(originalPos));
        env.removeNode(cellToMove3);
        env.removeNode(c4);
    }

    /**
     * 
     */
    @Test
    public void testMoveNode4() {
        // test4
        final CellWithCircularArea cellToMove4 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove4, originalPos);
        final Position p5 = new Continuous2DEuclidean(0.2, FastMath.sqrt(FastMath.pow(cellToMove4.getDiameter(), 2) - FastMath.pow(0.2, 2)));
        final CellWithCircularArea c5 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c5, p5);
        env.moveNode(cellToMove4, new Continuous2DEuclidean(80, 0));
        assertFalse("cellToMove4 is in position: " + env.getPosition(cellToMove4).toString(),
                env.getPosition(cellToMove4).equals(new Continuous2DEuclidean(80, 0)));
        env.removeNode(cellToMove4);
        env.removeNode(c5);
    }

    /**
     * 
     */
    @Test
    public void testMoveNode5() {
        // test5
        final CellWithCircularArea cellToMove5 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove5, originalPos);
        final Position p6 = new Continuous2DEuclidean(20, STANDARD_DIAMETER);
        final CellWithCircularArea c6 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c6, p6);
        env.moveNode(cellToMove5, new Continuous2DEuclidean(80, 0));
        assertTrue("cellToMove5 is in position: " + env.getPosition(cellToMove5).toString(),
                env.getPosition(cellToMove5).equals(new Continuous2DEuclidean(80, 0)));
        env.removeNode(cellToMove5);
        env.removeNode(c6);
    }

    /**
     * 
     */
    @Test
    public void testMoveNode6() {
        // test6
        final CellWithCircularArea cellToMove6 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove6, originalPos);
        final Position p7 = new Continuous2DEuclidean(-40, 0);
        final CellWithCircularArea c7 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c7, p7);
        env.moveNode(cellToMove6, new Continuous2DEuclidean(-80, 0));
        assertTrue("cellToMove6 is in position: " + env.getPosition(cellToMove6).toString(),
                env.getPosition(cellToMove6).equals(new Continuous2DEuclidean(-30, 0)));
        env.removeNode(cellToMove6);
        env.removeNode(c7);
    }

    /**
     * 
     */
    @Test
    public void testMoveNode7() {
        // test7
        final CellWithCircularArea cellToMove7 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove7, originalPos);
        final Position p8 = new Continuous2DEuclidean(40, 40);
        final CellWithCircularArea c8 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c8, p8);
        env.moveNode(cellToMove7, new Continuous2DEuclidean(80, 80));
        assertTrue("cellToMove7 is in position: " + env.getPosition(cellToMove7).toString(),
                env.getPosition(cellToMove7).equals(new Continuous2DEuclidean(40 - (STANDARD_DIAMETER / FastMath.sqrt(2)), 40 - (STANDARD_DIAMETER / FastMath.sqrt(2)))));
        env.removeNode(cellToMove7);
        env.removeNode(c8);
    }

    /**
     * 
     */
    public void testMoveNode8() {
        // test8
        final CellWithCircularArea cellToMove8 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(cellToMove8, originalPos);
        final Position p9 = new Continuous2DEuclidean(-40, -40);
        final CellWithCircularArea c9 = new CellNodeImpl(env, STANDARD_DIAMETER);
        env.addNode(c9, p9);
        env.moveNode(cellToMove8, new Continuous2DEuclidean(-80, -80));
        assertTrue("cellToMove8 is in position: " + env.getPosition(cellToMove8).toString(),
                env.getPosition(cellToMove8).equals(new Continuous2DEuclidean(-40 + (STANDARD_DIAMETER / FastMath.sqrt(2)), -40 + (STANDARD_DIAMETER / FastMath.sqrt(2)))));
        env.removeNode(cellToMove8);
        env.removeNode(c9);
    }

    // TODO test with different cells

    /**
     * 
     */
    @Test
    public void testAddDifferentDiam1() {
        env.addNode(ng1, originalPos);
        
        final Position p1 = new Continuous2DEuclidean(STANDARD_DIAMETER, 0);
        final Position p2 = new Continuous2DEuclidean(0, STANDARD_DIAMETER);
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
     * 
     */
    @Test
    public void testAddDifferentDiam2() {
        env.addNode(np1, originalPos);

        final Position p1 = new Continuous2DEuclidean(STANDARD_DIAMETER, 0);
        final Position p2 = new Continuous2DEuclidean(2 * STANDARD_DIAMETER, 0);
        final Position p3 = new Continuous2DEuclidean(3 * STANDARD_DIAMETER, 0);
        env.addNode(ng1, p1);
        env.addNode(ng2, p2);
        env.addNode(ng3, p3);
        assertNull("ng1 not in pos null; it's in pos " + env.getPosition(ng1),
                env.getPosition(ng1));
        assertTrue("ng2 not in pos " + p2 + "; it's in pos " + env.getPosition(ng2),
                env.getPosition(ng2).equals(p2));
        assertNull("ng3 not in pos " + null + "; it's in pos " + env.getPosition(ng3),
                env.getPosition(ng3));
        env.removeNode(ng2);
        env.addNode(ng3, p3);
        assertTrue("ng3 not in pos " + p3 + "; it's in pos " + env.getPosition(ng3),
                env.getPosition(ng3).equals(p3));
    }

    /**
     * 
     */
    @Test
    public void testAddDifferentDiam3() {
        env.addNode(np1, originalPos);

        final Position p1 = new Continuous2DEuclidean(20 , 0);
        final Position p2 = new Continuous2DEuclidean(0, 15);
        final Position p3 = new Continuous2DEuclidean(STANDARD_DIAMETER, STANDARD_DIAMETER);
        env.addNode(ng1, p1);
        env.addNode(nm1, p2);
        env.addNode(nm2, p3);
        env.addNode(np2, p3);
        assertTrue("ng1 not in pos " + p1 + "; it's in pos " + env.getPosition(ng1),
                env.getPosition(ng1).equals(p1));
        assertTrue("nm1 not in pos " + p2 + "; it's in pos " + env.getPosition(nm1),
                env.getPosition(nm1).equals(p2));
        assertNull("nm2 not in pos " + null + "; it's in pos " + env.getPosition(nm2),
                env.getPosition(nm2));
        assertNull("np2 not in pos " + null + "; it's in pos " + env.getPosition(np2),
                env.getPosition(np2));
    }

    /**
     * 
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
     * 
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
     * 
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
                env.getPosition(cellToMove3).equals(new Continuous2DEuclidean(5, 0)));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove3, pd);
        assertTrue("cellToMove3 is in position: " + env.getPosition(cellToMove3),
                env.getPosition(cellToMove3).equals(new Continuous2DEuclidean(10, 0)));
    }

    /**
     * 
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
                env.getPosition(cellToMove4).equals(new Continuous2DEuclidean(-5, 0)));
        env.removeNode(ng1);
        env.addNode(nm1, p1);
        env.moveNode(cellToMove4, pd);
        assertTrue("cellToMove4 is in position: " + env.getPosition(cellToMove4),
                env.getPosition(cellToMove4).equals(new Continuous2DEuclidean(-10, 0)));
    }

    /**
     * 
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
     * 
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
     * 
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
     * 
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

}
