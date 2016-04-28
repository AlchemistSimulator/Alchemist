package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.interfaces.Environment;

/**
 * A series of tests checking that our Yaml Loader is working as expected.
 */
public class TestYAMLLoader {

    /**
     * Basic loading capabilities.
     */
    @Test
    public void testBase() {
        testNoVar("/testbase.yml");
    }

    /**
     * Test the ability to load a Protelis module from classpath.
     */
    @Test
    public void testLoadProtelisModule() {
        testNoVar("/test00.yml");
    }

    /**
     * Test the ability to inject variables.
     */
    @Test
    public void testLoadWIthVariable() {
        final Map<String, Double> map = Maps.newLinkedHashMap();
        map.put("testVar", 10d);
        testLoading("/test00.yml", map);
    }

    private static void testNoVar(final String resource) {
        testLoading(resource, Collections.emptyMap());
    }

    private static <T> void testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = TestYAMLLoader.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource " + resource, res);
        final Environment<T> env = new YamlLoader(res).getWith(vars);
        final Simulation<T> sim = new Engine<>(env, 10000);
        sim.addCommand(new Engine.StateCommand<T>().run().build());
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
    }

}
