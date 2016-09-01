package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironmentNoOverlap;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

public class TestChemiotaxis {
    
    private Environment<Double> env;
    private CellNode cellNode1;
    private EnvironmentNode envNode1;
    private EnvironmentNode envNode2;
    private EnvironmentNode envNode3;
    private EnvironmentNode envNode4;
    Biomolecule biomolA = new Biomolecule("A");
    Incarnation<Double> inc = new BiochemistryIncarnation();
    private RandomGenerator rand;
    private TimeDistribution<Double> time;  
    
    @Before
    public void setUp() {
        env = new BioRect2DEnvironmentNoOverlap();
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.EuclideanDistance<>(2));
        envNode1 = new EnvironmentNodeImpl(env);
        envNode2 = new EnvironmentNodeImpl(env);
        envNode3 = new EnvironmentNodeImpl(env);
        envNode4 = new EnvironmentNodeImpl(env);
        cellNode1 = new CellNodeImpl(env);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }
    
    @Test
    public void testChemiotacticPolarization1() {
        env.addNode(envNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(envNode2, new Continuous2DEuclidean(1, 0));
        env.addNode(envNode3, new Continuous2DEuclidean(0, 1));
        env.addNode(envNode4, new Continuous2DEuclidean(1, 1));
        env.addNode(cellNode1, new Continuous2DEuclidean(0.5, 0.5));
        envNode4.setConcentration(biomolA, 10d);
        envNode2.setConcentration(biomolA, 5d);
        envNode3.setConcentration(biomolA, 5d);
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemiotacticPolarization(A, true)]");
        r.execute();
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(0),
                FastMath.sqrt(0.5),
                0.000000000000001
                );
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(1),
                FastMath.sqrt(0.5),
                0.000000000000001
                );
    }
    
    @Test
    public void testChemiotacticPolarization2() {
        env.addNode(envNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(envNode2, new Continuous2DEuclidean(1, 0));
        env.addNode(envNode3, new Continuous2DEuclidean(0, 1));
        env.addNode(envNode4, new Continuous2DEuclidean(3, 3));
        env.addNode(cellNode1, new Continuous2DEuclidean(0.5, 0.5));
        envNode4.setConcentration(biomolA, 10d);
        envNode2.setConcentration(biomolA, 5d);
        envNode3.setConcentration(biomolA, 5d);
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemiotacticPolarization(A, true)]");
        r.execute();
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(0),
                0,
                0.000000000000001
                );
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(1),
                0,
                0.000000000000001
                );
    }
    
    @Test
    public void testChemiotacticPolarization3() {
        env.addNode(envNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(envNode2, new Continuous2DEuclidean(1, 0));
        env.addNode(envNode3, new Continuous2DEuclidean(0, 1));
        env.addNode(envNode4, new Continuous2DEuclidean(1, 1));
        env.addNode(cellNode1, new Continuous2DEuclidean(0.5, 0.5));
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemiotacticPolarization(A, true)]");
        r.execute();
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(0),
                0,
                0.000000000000001
                );
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(1),
                0,
                0.000000000000001
                );
    }
    
    @Test
    public void testChemiotacticPolarization4() {
        env.addNode(cellNode1, new Continuous2DEuclidean(0.5, 0.5));
        final Reaction<Double> r = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemiotacticPolarization(A, true)]");
        r.execute();
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(0),
                0,
                0.000000000000001
                );
        assertEquals("the polarization is = " + cellNode1.getPolarizationVersor(), 
                cellNode1.getPolarizationVersor().getCoordinate(1),
                0,
                0.000000000000001
                );
    }
    
    @Test
    public void testChemiotacticMove1() {
        env.addNode(envNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(envNode2, new Continuous2DEuclidean(1, 0));
        env.addNode(envNode3, new Continuous2DEuclidean(0, 1));
        env.addNode(envNode4, new Continuous2DEuclidean(1, 1));
        env.addNode(cellNode1, new Continuous2DEuclidean(0.5, 0.5));
        envNode4.setConcentration(biomolA, 10d);
        envNode2.setConcentration(biomolA, 5d);
        envNode3.setConcentration(biomolA, 5d);
        final Reaction<Double> r1 = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemiotacticPolarization(A, true)]");
        final Reaction<Double> r2 = inc.createReaction(rand, env, cellNode1, time, "[] --> [CellMove(false, 1)]");
        r1.execute();
        r2.execute();
        assertEquals("the cell is in pos = " + env.getPosition(cellNode1), 
                new Continuous2DEuclidean(0.5 + FastMath.sqrt(0.5), 0.5 + FastMath.sqrt(0.5)),
                env.getPosition(cellNode1)
                );
    }
    
    @Test
    public void testChemiotacticMove2() {
        env.addNode(envNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(envNode2, new Continuous2DEuclidean(1, 0));
        env.addNode(envNode3, new Continuous2DEuclidean(0, 1));
        env.addNode(envNode4, new Continuous2DEuclidean(1, 1));
        env.addNode(cellNode1, new Continuous2DEuclidean(1, 1));
        envNode4.setConcentration(biomolA, 10d);
        envNode2.setConcentration(biomolA, 5d);
        envNode3.setConcentration(biomolA, 5d);
        final Reaction<Double> r1 = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemiotacticPolarization(A, true)]");
        final Reaction<Double> r2 = inc.createReaction(rand, env, cellNode1, time, "[] --> [CellMove(false, 1)]");
        r1.execute();
        r2.execute();
        assertEquals("the cell is in pos = " + env.getPosition(cellNode1), 
                1,
                env.getPosition(cellNode1).getCoordinate(0),
                0.000000000001
                );
        assertEquals("the cell is in pos = " + env.getPosition(cellNode1), 
                1,
                env.getPosition(cellNode1).getCoordinate(1),
                0.000000000001
                );
    }
    
    @Test
    public void testChemiotacticMove3() {
        env.addNode(envNode1, new Continuous2DEuclidean(0, 0));
        env.addNode(envNode2, new Continuous2DEuclidean(1, 0));
        env.addNode(envNode3, new Continuous2DEuclidean(0, 1));
        env.addNode(envNode4, new Continuous2DEuclidean(1, 1));
        env.addNode(cellNode1, new Continuous2DEuclidean(0.5, 0.5));
        envNode4.setConcentration(biomolA, 10d);
        envNode2.setConcentration(biomolA, 10d);
        envNode3.setConcentration(biomolA, 10d);
        envNode1.setConcentration(biomolA, 10d);
        final Reaction<Double> r1 = inc.createReaction(rand, env, cellNode1, time, "[] --> [ChemiotacticPolarization(A, true)]");
        final Reaction<Double> r2 = inc.createReaction(rand, env, cellNode1, time, "[] --> [CellMove(false, 1)]");
        r1.execute();
        r2.execute();
        r2.execute();
        assertEquals("the cell is in pos = " + env.getPosition(cellNode1), 
                0.5,
                env.getPosition(cellNode1).getCoordinate(0),
                0.000000000001
                );
        assertEquals("the cell is in pos = " + env.getPosition(cellNode1), 
                0.5,
                env.getPosition(cellNode1).getCoordinate(1),
                0.000000000001
                );
    }
}
