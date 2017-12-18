package it.unibo.alchemist.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
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
        Assert.assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        final GeneralSimulationConfig<?> gsc = new LocalGeneralSimulationConfig<>(l, 0, null);
        Assert.assertEquals(gsc.getDependencies().size(), 2);
        try {
            Assert.assertArrayEquals(gsc.getDependencies().get(DEPENDENCY_FILE), Files.readAllBytes(Paths.get(ResourceLoader.getResource(DEPENDENCY_FILE).toURI())));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
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
        Assert.assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        final GeneralSimulationConfig<?> gsc = new LocalGeneralSimulationConfig<>(l, 0, null);
        Assert.assertEquals(gsc.getDependencies().size(), 2);
        File test = null;
        try (WorkingDirectory wd = new WorkingDirectory()) {
            test = new File(wd.getFileAbsolutePath("nothing")).getParentFile();
            Assert.assertTrue(test.exists());
            wd.writeFiles(gsc.getDependencies());
            final File newFile = new File(wd.getFileAbsolutePath("test.txt"));
            newFile.createNewFile();
            ResourceLoader.addURL(wd.getDirectoryUrl());
            Assert.assertNotNull(ResourceLoader.getResource("test.txt"));
        }
        Assert.assertFalse(test.exists());
    }


    private Loader getLoader(final InputStream yaml) {
        return new YamlLoader(yaml);
    }

}
