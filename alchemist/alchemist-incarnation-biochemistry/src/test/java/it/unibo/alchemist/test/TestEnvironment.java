package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.junit.Test;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.interfaces.Environment;

public class TestEnvironment {

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
        assertTrue("conA = " + conA, conA == 1000);
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
        final double conA = (double) testNoVar("/testEnv3.yml").getNodes().stream()
                .parallel()
                .filter(n -> n.getClass().equals(CellNodeImpl.class) )
                .findAny()
                .get()
                .getConcentration(new Biomolecule("A"));
        assertTrue("conA = " + conA, conA == 1000);
    }

    private static <T> Environment<T> testNoVar(final String resource) {
        return testLoading(resource, Collections.emptyMap());
    }

    private static <T> Environment<T> testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = TestEnvironment.class.getResourceAsStream(resource);
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
