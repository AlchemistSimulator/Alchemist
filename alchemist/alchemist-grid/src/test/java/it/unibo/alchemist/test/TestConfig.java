package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.LocalGeneralSimulationConfig;
import it.unibo.alchemist.grid.util.WorkingDirectory;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;

/**
 */
public class TestConfig {
    private static final String DEPENDENCY_FILE = "config/dependencies_test.txt";


    /**
     * 
     */
    @Test
    public void testGeneralSimulationConfig() {
        final String resource = "config/00-dependencies.yml";
        final InputStream yaml = ResourceLoader.getResourceAsStream(resource);
        assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        //TODO aggiungi test per endTime e endStep???
        final GeneralSimulationConfig<?> gsc = new LocalGeneralSimulationConfig<>(l, 0, null);
        assertEquals(gsc.getDependencies().size(), 2);
        try {
            assertEquals(gsc.getDependencies().get(DEPENDENCY_FILE), new String(Files.readAllBytes(Paths.get(ResourceLoader.getResource(DEPENDENCY_FILE).toURI()))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * @throws IOException 
     * @throws ReflectiveOperationException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * 
     */
    @Test
    public void testWorkingDirectory() throws IOException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, ReflectiveOperationException {
        final String resource = "config/00-dependencies.yml";
        final InputStream yaml = ResourceLoader.getResourceAsStream(resource);
        assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        final GeneralSimulationConfig<?> gsc = new LocalGeneralSimulationConfig<>(l, 0, null);
        assertEquals(gsc.getDependencies().size(), 2);
        File test = null;
        try (WorkingDirectory wd = new WorkingDirectory()) {
            test = new File(wd.getFileAbsolutePath("nothing")).getParentFile();
            assertTrue(test.exists());
            wd.writeFiles(gsc.getDependencies());
            final File newFile = new File(wd.getFileAbsolutePath("test.txt"));
            newFile.createNewFile();
            wd.addToClasspath();
            assertNotNull(Thread.currentThread().getContextClassLoader().getResource("test.txt"));
        }
        assertFalse(test.exists());
    }


    private Loader getLoader(final InputStream yaml) {
        return new YamlLoader(yaml);
    }

}
