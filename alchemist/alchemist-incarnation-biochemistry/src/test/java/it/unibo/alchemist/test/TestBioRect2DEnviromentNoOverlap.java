package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.CellNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ICellNodeWithShape;
import it.unibo.alchemist.model.interfaces.Position;
/**
 *
 */
public class TestBioRect2DEnviromentNoOverlap {

    private Environment<Double> env;

    @Before
    public void setUp() {
        env = new BioRect2DEnvironmentNoOverlap();
        env.setLinkingRule(new NoLinks<>());
    }

    @Test
    public void testAddNode() {
        final ICellNodeWithShape n1 = new CellNode(env);
        final ICellNodeWithShape n2 = new CellNode(env);
        final ICellNodeWithShape n3 = new CellNode(env);
        final ICellNodeWithShape n4 = new CellNode(env);
        final ICellNodeWithShape n5 = new CellNode(env);
        final ICellNodeWithShape n6 = new CellNode(env);
        final ICellNodeWithShape n7 = new CellNode(env);
        final ICellNodeWithShape n8 = new CellNode(env);
        final ICellNodeWithShape n9 = new CellNode(env);
        final ICellNodeWithShape n10 = new CellNode(env);
        final ICellNodeWithShape n11 = new CellNode(env);

        final Position p1 = new Continuous2DEuclidean(0, 0);
        final Position p2 = new Continuous2DEuclidean(10, 0);
        env.addNode(n1, p1);
        env.addNode(n2, p2);

        final Position p3 = new Continuous2DEuclidean(0, 20); //ok
        env.addNode(n3, p3);
        assertTrue("n3 not in pos " + p3.toString() + "; it's in pos " + env.getPosition(n3),
                env.getPosition(n3).equals(p3));
        env.removeNode(n3);
        final Position p4 = new Continuous2DEuclidean(0, 10); //ok
        env.addNode(n4, p4);
        assertTrue("n4 not in pos " + p4.toString() + "; it's in pos " + env.getPosition(n4),
                env.getPosition(n4).equals(p4));
        env.removeNode(n4);
        final Position p5 = new Continuous2DEuclidean(0, 5); // fail
        env.addNode(n5, p5);
        assertTrue("n5 not in pos " + null + "; it's in pos " + env.getPosition(n5),
                env.getPosition(n5) == (null));
        final Position p6 = new Continuous2DEuclidean(5, 0); // fail
        env.addNode(n6, p6);
        assertTrue("n6 not in pos " + null + "; it's in pos " + env.getPosition(n6),
                env.getPosition(n6) == (null));
        final Position p7 = new Continuous2DEuclidean(0, 0); //fail
        env.addNode(n7, p7);
        assertTrue("n7 not in pos " + null + "; it's in pos " + env.getPosition(n7),
                env.getPosition(n7) == null);
        final Position p8 = new Continuous2DEuclidean(10, 0); //fail
        env.addNode(n8, p8);
        assertTrue("n8 not in pos " + null + "; it's in pos " + env.getPosition(n8),
                env.getPosition(n8) == null);
        final Position p9 = new Continuous2DEuclidean(20, 0); //ok
        env.addNode(n9, p9);
        assertTrue("n9 not in pos " + p9.toString() + "; it's in pos " + env.getPosition(n9),
                env.getPosition(n9).equals(p9));
        env.removeNode(n9);
        final Position p10 = new Continuous2DEuclidean(2.5, 2.5); //fail
        env.addNode(n10, p10);
        assertTrue("n10 not in pos " + null + "; it's in pos " + env.getPosition(n10),
                env.getPosition(n10) == (null));
        final Position p11 =  new Continuous2DEuclidean(7.5, -2.5); //fail
        env.addNode(n11, p11);
        assertTrue("n11 not in pos " + null + "; it's in pos " + env.getPosition(n11),
                env.getPosition(n11) == (null));

        env.removeNode(n1);
        env.removeNode(n2);
    }

    @Test
    public void testMoveNode1() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test1
        final ICellNodeWithShape cellToMove1 = new CellNode(env);
        env.addNode(cellToMove1, originalPos);
        final Position p1 = new Continuous2DEuclidean(40, 0);
        final ICellNodeWithShape c1 = new CellNode(env);
        env.addNode(c1, p1);
        env.moveNode(cellToMove1, new Continuous2DEuclidean(80, 0));
        assertTrue("cellToMove1 is in position: " + env.getPosition(cellToMove1).toString(),
                env.getPosition(cellToMove1).equals(new Continuous2DEuclidean(30, 0)));
        env.removeNode(cellToMove1);
        env.removeNode(c1);
    }

    @Test
    public void testMoveNode2() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test2 
        final ICellNodeWithShape cellToMove2 = new CellNode(env);
        env.addNode(cellToMove2, originalPos);
        final Position p2 = new Continuous2DEuclidean(40, 5);
        final Position p3 = new Continuous2DEuclidean(40, -5);
        final ICellNodeWithShape c2 = new CellNode(env);
        final ICellNodeWithShape c3 = new CellNode(env);
        env.addNode(c2, p2);
        env.addNode(c3, p3);
        env.moveNode(cellToMove2, new Continuous2DEuclidean(80, 0));
        assertTrue("cellToMove2 is in position: " + env.getPosition(cellToMove2).toString(),
                env.getPosition(cellToMove2).equals(new Continuous2DEuclidean(40 - FastMath.sqrt(75), 0)));
        env.removeNode(cellToMove2);
        env.removeNode(c2);
        env.removeNode(c3);
    }

    @Test
    public void testMoveNode3() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test3
        final ICellNodeWithShape cellToMove3 = new CellNode(env);
        env.addNode(cellToMove3, originalPos);
        final Position p4 = new Continuous2DEuclidean(10, 0);
        final ICellNodeWithShape c4 = new CellNode(env);
        env.addNode(c4, p4);
        env.moveNode(cellToMove3, new Continuous2DEuclidean(80, 0));
        assertTrue("cellToMove3 is in position: " + env.getPosition(cellToMove3).toString(),
                env.getPosition(cellToMove3).equals(originalPos));
        env.removeNode(cellToMove3);
        env.removeNode(c4);
    }

    @Test
    public void testMoveNode4() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test4
        final ICellNodeWithShape cellToMove4 = new CellNode(env);
        env.addNode(cellToMove4, originalPos);
        final Position p5 = new Continuous2DEuclidean(0.2, FastMath.sqrt(FastMath.pow(cellToMove4.getShape().getMaxRange(), 2) - FastMath.pow(0.2, 2)));
        final ICellNodeWithShape c5 = new CellNode(env);
        env.addNode(c5, p5);
        env.moveNode(cellToMove4, new Continuous2DEuclidean(80, 0));
        assertFalse("cellToMove4 is in position: " + env.getPosition(cellToMove4).toString(),
                env.getPosition(cellToMove4).equals(new Continuous2DEuclidean(80, 0)));
        env.removeNode(cellToMove4);
        env.removeNode(c5);
    }

    @Test
    public void testMoveNode5() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test5
        final ICellNodeWithShape cellToMove5 = new CellNode(env);
        env.addNode(cellToMove5, originalPos);
        final Position p6 = new Continuous2DEuclidean(20, 10);
        final ICellNodeWithShape c6 = new CellNode(env);
        env.addNode(c6, p6);
        env.moveNode(cellToMove5, new Continuous2DEuclidean(80, 0));
        assertTrue("cellToMove5 is in position: " + env.getPosition(cellToMove5).toString(),
                env.getPosition(cellToMove5).equals(new Continuous2DEuclidean(80, 0)));
        env.removeNode(cellToMove5);
        env.removeNode(c6);
    }

    @Test
    public void testMoveNode6() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test1
        final ICellNodeWithShape cellToMove6 = new CellNode(env);
        env.addNode(cellToMove6, originalPos);
        final Position p7 = new Continuous2DEuclidean(-40, 0);
        final ICellNodeWithShape c7 = new CellNode(env);
        env.addNode(c7, p7);
        env.moveNode(cellToMove6, new Continuous2DEuclidean(-80, 0));
        assertTrue("cellToMove6 is in position: " + env.getPosition(cellToMove6).toString(),
                env.getPosition(cellToMove6).equals(new Continuous2DEuclidean(-30, 0)));
        env.removeNode(cellToMove6);
        env.removeNode(c7);
    }

    @Test
    public void testMoveNode7() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test1
        final ICellNodeWithShape cellToMove7 = new CellNode(env);
        env.addNode(cellToMove7, originalPos);
        final Position p8 = new Continuous2DEuclidean(40, 40);
        final ICellNodeWithShape c8 = new CellNode(env);
        env.addNode(c8, p8);
        env.moveNode(cellToMove7, new Continuous2DEuclidean(80, 80));
        assertTrue("cellToMove7 is in position: " + env.getPosition(cellToMove7).toString(),
                env.getPosition(cellToMove7).equals(new Continuous2DEuclidean(40 - (10 / FastMath.sqrt(2)), 40 - (10 / FastMath.sqrt(2)))));
        env.removeNode(cellToMove7);
        env.removeNode(c8);
    }

    public void testMoveNode8() {
        final Position originalPos = new Continuous2DEuclidean(0, 0);
        // test1
        final ICellNodeWithShape cellToMove8 = new CellNode(env);
        env.addNode(cellToMove8, originalPos);
        final Position p9 = new Continuous2DEuclidean(-40, -40);
        final ICellNodeWithShape c9 = new CellNode(env);
        env.addNode(c9, p9);
        env.moveNode(cellToMove8, new Continuous2DEuclidean(-80, -80));
        assertTrue("cellToMove8 is in position: " + env.getPosition(cellToMove8).toString(),
                env.getPosition(cellToMove8).equals(new Continuous2DEuclidean(-40 + (10 / FastMath.sqrt(2)), -40 + (10 / FastMath.sqrt(2)))));
        env.removeNode(cellToMove8);
        env.removeNode(c9);
    }
}
