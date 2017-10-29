package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.LocalGeneralSimulationConfig;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;

/**
 */
public class TestConfig {
    private static final String DEPENDENCY_FILE = "/config/dependencies_test.txt";


    /**
     * 
     */
    @Test
    public void testGeneralSimulationConfig() {
        final String resource = "/config/00-dependencies.yml";
        final InputStream yaml = TestConfig.class.getResourceAsStream(resource);
        assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        //TODO aggiungi test per endTime e endStep???
        final GeneralSimulationConfig<?> gsc = new LocalGeneralSimulationConfig<>(l, 0, null);
        assertEquals(gsc.getYamlDependencies().size(), 2);
        try {
            assertEquals(gsc.getYamlDependencies().get(DEPENDENCY_FILE), new String(Files.readAllBytes(Paths.get(TestConfig.class.getResource(DEPENDENCY_FILE).toURI()))));
            assertEquals(gsc.getYaml(), new String(Files.readAllBytes(Paths.get(TestConfig.class.getResource(resource).toURI()))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    private Loader getLoader(final InputStream yaml) {
        return new YamlLoader(yaml);
    }

}
