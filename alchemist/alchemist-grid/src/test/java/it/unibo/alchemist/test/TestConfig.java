/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.LocalGeneralSimulationConfig;
import it.unibo.alchemist.grid.util.WorkingDirectory;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 */
public class TestConfig {
    private static final String DEPENDENCY_FILE = "config/dependencies_test.txt";


    /**
     * @throws URISyntaxException indicates failure
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testGeneralSimulationConfig() throws URISyntaxException, IOException {
        final String resource = "config/00-dependencies.yml";
        final InputStream yaml = ResourceLoader.getResourceAsStream(resource);
        Assertions.assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        final GeneralSimulationConfig gsc = new LocalGeneralSimulationConfig(l, 0, DoubleTime.INFINITE_TIME);
        Assertions.assertEquals(gsc.getDependencies().size(), 2);
        Assertions.assertArrayEquals(gsc.getDependencies().get(DEPENDENCY_FILE), Files.readAllBytes(Paths.get(ResourceLoader.getResource(DEPENDENCY_FILE).toURI())));
    }

    /**
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testWorkingDirectory() throws IOException {
        final String resource = "config/00-dependencies.yml";
        final InputStream yaml = ResourceLoader.getResourceAsStream(resource);
        Assertions.assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        final GeneralSimulationConfig gsc = new LocalGeneralSimulationConfig(l, 0, DoubleTime.INFINITE_TIME);
        Assertions.assertEquals(gsc.getDependencies().size(), 2);
        final File test;
        try (WorkingDirectory wd = new WorkingDirectory()) {
            test = new File(wd.getFileAbsolutePath("nothing")).getParentFile();
            Assertions.assertTrue(test.exists());
            wd.writeFiles(gsc.getDependencies());
            final File newFile = new File(wd.getFileAbsolutePath("test.txt"));
            if (newFile.exists() || newFile.createNewFile()) {
                ResourceLoader.addURL(wd.getDirectoryUrl());
                Assertions.assertNotNull(ResourceLoader.getResource("test.txt"));
            } else {
                Assertions.fail();
            }
        }
        Assertions.assertFalse(test.exists());
    }


    private Loader getLoader(final InputStream yaml) {
        return new YamlLoader(yaml);
    }

}
