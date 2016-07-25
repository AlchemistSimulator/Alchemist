package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Molecule;

/**
 * Test implementation of extra-cellular environment  created with EnvironmentNodes.
 *
 */
public class TestEnvironmentNodes {

    /**
     * test a simple reaction "[A] --> [A in env]".
     */
    @Test
    public void test1() {
        final Environment<Double> env = new BioRect2DEnvironment();
        final CellNode cellNode = new CellNodeImpl(env);
        final EnvironmentNode envNode = new EnvironmentNodeImpl(env);
        final MersenneTwister rand = new MersenneTwister();
        final Molecule a = new Biomolecule("A");
        cellNode.addReaction(new BiochemistryIncarnation().createReaction(
                rand, env, cellNode, new ExponentialTime<>(1, rand), "[A] --> [A in env]"
                ));
        cellNode.setConcentration(a, 1000.0);
        env.setLinkingRule(new it.unibo.alchemist.model.implementations.linkingrules.EuclideanDistance<>(2));
        env.addNode(cellNode, new Continuous2DEuclidean(0, 0));
        env.addNode(envNode, new Continuous2DEuclidean(0, 1));
        final Simulation<Double> sim = new Engine<>(env, 10000);
        sim.addCommand(new Engine.StateCommand<Double>().run().build());
        try {
            SwingUtilities.invokeAndWait(sim);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(envNode.getConcentration(a) == 1000);
    }

    /**
     * Simple interaction between a CellNode and an EnviromentalNode.
     * Test transport of a molecule from cell to env.
     */
    @Test
    public void testEnv1() {
        final double conA = (double) testNoVar("/testEnv1.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .findFirst()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue(conA == 1000);
    }

    /**
     * Simple interaction between a CellNode and 4 EnviromentalNodes.
     */
    @Test
    public void testEnv2() {
        testNoVar("/testEnv2.yml").getNodes().stream()
        .parallel()
        .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
        .mapToDouble(n -> (double) n.getConcentration(new Biomolecule("A")))
        .forEach(c -> assertFalse("concentration is " + c, c == 0 || c == 1000));
    }

    /**
     * Simple interaction between a CellNode and an EnviromentalNode.
     * Test transport of a molecule from env to cell.
     */
    @Test
    public void testEnv3() {
        final double conAInCell = (double) testNoVar("/testEnv3.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(CellNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) testNoVar("/testEnv3.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue(conAInCell == 1000);
        assertTrue(conAInEnv == 0);
    }

    /**
     * The same as testEnv3, missing initial concentration of A in env.
     * So there's no A in env and in cell both.
     */
    @Test
    public void testEnv4() {
        final double conAInCell = (double) testNoVar("/testEnv4.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(CellNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv = (double) testNoVar("/testEnv4.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(EnvironmentNodeImpl.class))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue("conAInCell = " + conAInCell + " ; conAInEnv = " + conAInEnv, conAInCell == 0 && conAInEnv == 0);
    }

    private static <T> Environment<T> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }

    private static <T> Environment<T> testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = TestEnvironmentNodes.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T> env = new YamlLoader(res).getWith(vars);
        final Simulation<T> sim = new Engine<>(env, 10000);
        sim.addCommand(new Engine.StateCommand<T>().run().build());
        try {
            SwingUtilities.invokeAndWait(sim);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
        return env;
    }

}
