package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.reactions.BiochemicalReactionBuilder;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * Test implementation of extra-cellular environment  created with EnvironmentNodes.
 *
 */
public class TestEnvironmentNodes {

    private static final Logger L = LoggerFactory.getLogger(BiochemicalReactionBuilder.class);

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
            L.error(e.getMessage());
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
        assertEquals("conA = " + conA, conA, 1000, Double.MIN_VALUE);
    }

    /**
     * Simple interaction between a CellNode and 4 EnviromentalNodes.
     */
    @Test
    public void testEnv2() {
        final Environment<Double> env = testNoVar("/testEnv2.yml");
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
        assertTrue(conAInNearest == 1000);
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
        assertTrue("conAInCell = " + conAInCell, conAInCell == 1000);
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

    /**
     * test programming an environment node.
     */
    @Test
    public void testEnv5() {
        final Environment<Double> env = testNoVar("/testEnv5.yml");
        final double conAInEnv1 = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> env.getPosition(n).equals(new Continuous2DEuclidean(0, 0)))
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        final double conAInEnv2 = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> env.getPosition(n).equals(new Continuous2DEuclidean(1, 0)))
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
        testNoVar("/testEnv6.yml");
    }
    
    /**
     * test if neighbors are selected correctly.
     */
    @Test
    public void testEnv7() {
        final Environment<Double> env = testNoVar("/testEnv7.yml");
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
        assertTrue("conAInCell = " + conAInCell + " ; conAInEnv = " + conAInEnv, conAInCell == 2000 && conAInEnv == 0);
    }

    /**
     * test if neighbors are selected correctly.
     */
    @Test
    public void testEnv8() {
        final Environment<Double> env = testNoVar("/testEnv8.yml");
        final double conAInCell = (double) env.getNodes().stream()
                .parallel()
                .filter(n -> n instanceof CellNode)
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue("conAInCell = " + conAInCell, conAInCell == 1000);
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
        sim.run();
        return env;
    }

}
