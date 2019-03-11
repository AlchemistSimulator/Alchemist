/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;


import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
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
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

import static org.junit.Assert.*;

/**
 * Test implementation of extra-cellular environment  created with EnvironmentNodes.
 *
 */

public class TestEnvironmentNodes {

    private static final double PRECISION = 0.000000000001;
    private static final Incarnation<Double, Euclidean2DPosition> INCARNATION = new BiochemistryIncarnation<>();
    private static final String CON_A_IN_CELL = "conAInCell = ";
    /**
     * test a simple reaction "[A] --> [A in env]".
     */
    @Test
    public void test1() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        final CellNode<Euclidean2DPosition> cellNode = new CellNodeImpl<>(env);
        final EnvironmentNode envNode = new EnvironmentNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        cellNode.addReaction(INCARNATION.createReaction(
                rand, env, cellNode, new ExponentialTime<>(1, rand), "[A] --> [A in env]" //NOPMD
                ));
        cellNode.setConcentration(a, 1000.0);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        env.addNode(cellNode, new Euclidean2DPosition(0, 0));
        env.addNode(envNode, new Euclidean2DPosition(0, 1));
        final Simulation<?, ?> sim = new Engine<>(env, 10000);
        sim.play();
        sim.run();
        assertEquals(envNode.getConcentration(a), 1000, PRECISION);
    }

    /**
     * test a simple reaction "[A] --> [A in env]".
     */
    @Test
    public void test2() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        final EnvironmentNode envNode1 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        envNode1.addReaction(INCARNATION.createReaction(
                rand, env, envNode1, new ExponentialTime<>(1, rand), "[A] --> [A in env]"
                ));
        envNode1.setConcentration(a, 1000.0);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        env.addNode(envNode1, new Euclidean2DPosition(0, 0));
        env.addNode(envNode2, new Euclidean2DPosition(0, 1));
        final Simulation<?, ?> sim = new Engine<>(env, 10000);
        sim.play();
        sim.run();
        assertTrue(envNode2.getConcentration(a) == 1000 && envNode1.getConcentration(a) == 0);
    }

    /**
     * Test if env nodes are selected randomly.
     */
    @Test
    public void test3() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        final EnvironmentNode envNode1 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode3 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode4 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode5 = new EnvironmentNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        envNode1.addReaction(INCARNATION.createReaction(
                rand, env, envNode1, new ExponentialTime<>(1, rand), "[A] --> [A in env]"
                ));
        envNode1.setConcentration(a, 1000.0);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        env.addNode(envNode1, new Euclidean2DPosition(0, 0));
        env.addNode(envNode2, new Euclidean2DPosition(0, 1));
        env.addNode(envNode3, new Euclidean2DPosition(1, 0));
        env.addNode(envNode4, new Euclidean2DPosition(-1, 0));
        env.addNode(envNode5, new Euclidean2DPosition(0, -1));
        final Simulation<?, ?> sim = new Engine<>(env, 10000);
        sim.play();
        sim.run();
        assertTrue(envNode2.getConcentration(a) != 0 
                && envNode1.getConcentration(a) == 0 
                && envNode3.getConcentration(a) != 0 
                && envNode4.getConcentration(a) != 0 
                && envNode5.getConcentration(a) != 0);
    }

    /**
     * Test if env nodes with same concentration are selected randomly.
     */
    @Test
    public void test4() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        final CellNode<Euclidean2DPosition> cellNode = new CellNodeImpl<>(env);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode3 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode4 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode5 = new EnvironmentNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        cellNode.addReaction(INCARNATION.createReaction(
                rand, env, cellNode, new ExponentialTime<>(1, rand), "[A] --> [A in env]"
                ));
        cellNode.setConcentration(a, 1000.0);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        env.addNode(cellNode, new Euclidean2DPosition(0, 0));
        env.addNode(envNode2, new Euclidean2DPosition(0, 1));
        env.addNode(envNode3, new Euclidean2DPosition(1, 0));
        env.addNode(envNode4, new Euclidean2DPosition(-1, 0));
        env.addNode(envNode5, new Euclidean2DPosition(0, -1));
        final Simulation<?, ?> sim = new Engine<>(env, 10000);
        sim.play();
        sim.run();
        assertTrue(envNode2.getConcentration(a) != 0 
                && cellNode.getConcentration(a) == 0 
                && envNode3.getConcentration(a) != 0 
                && envNode4.getConcentration(a) != 0 
                && envNode5.getConcentration(a) != 0);
    }
 
    /**
     * Test if env nodes with same concentration are selected randomly.
     */
    @Test
    public void test5() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        final CellNode<Euclidean2DPosition> cellNode = new CellNodeImpl<>(env);
        final EnvironmentNode envNode1 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode2 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode3 = new EnvironmentNodeImpl(env);
        final EnvironmentNode envNode4 = new EnvironmentNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        cellNode.addReaction(INCARNATION.createReaction(
                rand, env, cellNode, new ExponentialTime<>(1, rand), "[A] --> [A in env]"));
        envNode1.addReaction(INCARNATION.createReaction(
                rand, env, envNode1, new ExponentialTime<>(1000, rand), "[A] --> [A in env]"));
        envNode2.addReaction(INCARNATION.createReaction(
                rand, env, envNode2, new ExponentialTime<>(1000, rand), "[A] --> [A in env]"));
        final double total = 1000.0;
        cellNode.setConcentration(a, 1000.0);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(1));
        final Euclidean2DPosition pos1 = new Euclidean2DPosition(0, -0.75);
        final Euclidean2DPosition pos2 = new Euclidean2DPosition(0, 0.75);
        final Euclidean2DPosition pos3 = new Euclidean2DPosition(0, 1.5);
        final Euclidean2DPosition pos4 = new Euclidean2DPosition(0, -1.5);
        env.addNode(cellNode, new Euclidean2DPosition(0, 0));
        env.addNode(envNode1, pos1);
        env.addNode(envNode2, pos2);
        env.addNode(envNode3, pos3);
        env.addNode(envNode4, pos4);
        final Simulation<?, ?> sim = new Engine<>(env, 10000);
        sim.play();
        sim.run();
        assertNotEquals(0.0, envNode3.getConcentration(a));
        assertNotEquals(0.0, envNode4.getConcentration(a));
        assertEquals(total, envNode3.getConcentration(a) + envNode4.getConcentration(a), 0.1);
    }

    /**
     * test a simple reaction "[A] --> [A in env]".
     */
    @Test
    public void test6() {
        final Environment<Double, Euclidean2DPosition> env = new BioRect2DEnvironment();
        final CellNode<Euclidean2DPosition> cellNode = new CellNodeImpl<>(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        cellNode.addReaction(INCARNATION.createReaction(
                rand, env, cellNode, new ExponentialTime<>(1, rand), "[A] --> [A in env]" //NOPMD
                ));
        cellNode.setConcentration(a, 1000.0);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance<>(2));
        env.addNode(cellNode, new Euclidean2DPosition(0, 0));
        final Simulation<?, ?> sim = new Engine<>(env, 10000);
        sim.play();
        sim.run();
        assertEquals(cellNode.getConcentration(a), 1000, PRECISION);
    }

    /**
     * Simple interaction between a CellNode and an EnviromentalNode.
     * Test transport of a molecule from cell to env.
     */
    @Test
    public void testEnv1() {
        final double conA = (double) testNoVar("testEnv1.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .findFirst()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertEquals("conA = " + conA, conA, 1000, Double.MIN_VALUE);
    }

    /**
     * Simple interaction between a CellNode and 4 EnviromentalNodes.
     */
    @Test
    public void testEnv2() {
        final Environment<Double, Euclidean2DPosition> env = testNoVar("testEnv2.yml");
        final Node<Double> center = env.getNodes().stream()
                .parallel()
                .filter(n -> n instanceof CellNode)
                .findAny()
                .get();
        final double conAInNearest = env.getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .min((n1, n2) -> Double.compare(
                        env.getPosition(n1).getDistanceTo(env.getPosition(center)), 
                        env.getPosition(n2).getDistanceTo(env.getPosition(center))
                        ))
                .get().getConcentration(new Biomolecule("A"));
        assertEquals(conAInNearest, 1000, PRECISION);
    }

    /**
     * Simple interaction between a CellNode and an EnviromentalNode.
     * Test transport of a molecule from env to cell.
     */
    @Test
    public void testEnv3() {
        final double conAInCell = (double) testNoVar("testEnv3.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(CellNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) testNoVar("testEnv3.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertEquals(CON_A_IN_CELL + conAInCell, conAInCell, 1000, PRECISION); 
        assertEquals(conAInEnv, 0, PRECISION);
    }

    /**
     * The same as testEnv3, missing initial concentration of A in env.
     * So there's no A in env and in cell both.
     */
    @Test
    public void testEnv4() {
        final double conAInCell = (double) testNoVar("testEnv4.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(CellNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) testNoVar("testEnv4.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue(CON_A_IN_CELL + conAInCell + " ; conAInEnv = " + conAInEnv, conAInCell == 0 && conAInEnv == 0);
    }

    /**
     * test programming an environment node.
     */
    @Test
    public void testEnv5() {
        final Environment<Double, Euclidean2DPosition> env = testNoVar("testEnv5.yml");
        final double conAInEnv1 = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> env.getPosition(n).equals(new Euclidean2DPosition(0, 0)))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv2 = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> env.getPosition(n).equals(new Euclidean2DPosition(1, 0)))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue("conAInEnv1 = " + conAInEnv1 + " ; conAInEnv2 = " + conAInEnv2, conAInEnv1 == 0 && conAInEnv2 == 1000);
    }

    /**
     * test initialization of a junction between an EnvironmentNode and a CellNode.
     * Now this will throw an UnsupportedOperationException
     */
    @Test (expected = UnsupportedOperationException.class)
    public void testEnv6() {
        testNoVar("testEnv6.yml");
    }

    /**
     * test if neighbors are selected correctly.
     */
    @Test
    public void testEnv7() {
        final Environment<Double, Euclidean2DPosition> env = testNoVar("testEnv7.yml");
        final double conAInCell = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> n instanceof CellNode)
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .mapToDouble(n -> n.getConcentration(new Biomolecule("A")))
                .sum();
        final double expectedConcInCell = 2000;
        final double expectedCOncInEnv = 0;
        assertTrue(CON_A_IN_CELL + conAInCell + " ; conAInEnv = " + conAInEnv, conAInCell == expectedConcInCell && conAInEnv == expectedCOncInEnv);
    }

    /**
     * test if neighbors are selected correctly.
     */
    @Test
    public void testEnv8() {
        final Environment<Double, Euclidean2DPosition> env = testNoVar("testEnv8.yml");
        final double conAInCell = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> n instanceof CellNode)
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertEquals(CON_A_IN_CELL + conAInCell, conAInCell, 1000, PRECISION);
    }

    private static <T, P extends Position<P>> Environment<T, P> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }

    private static <T, P extends Position<P>> Environment<T, P> testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = ResourceLoader.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T, P> env = new YamlLoader(res).getWith(vars);
        final Simulation<?, ?> sim = new Engine<>(env, 10000);
        sim.play();
        sim.run();
        return env;
    }

}
