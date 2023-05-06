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
import it.unibo.alchemist.loader.LoadAlchemist;
import it.unibo.alchemist.boundary.Loader;
import it.unibo.alchemist.model.Time;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 */
class TestConfig {
    private static final String DEPENDENCY_FILE = "config/dependencies_test.txt";

    /**
     * @throws URISyntaxException indicates failure
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testGeneralSimulationConfig() throws URISyntaxException, IOException {
        final String resource = "config/00-dependencies.yml";
        final var yaml = ResourceLoader.getResource(resource);
        Assertions.assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        final GeneralSimulationConfig gsc = new LocalGeneralSimulationConfig(l, 0, Time.INFINITY);
        Assertions.assertEquals(gsc.getDependencies().size(), 2);
        Assertions.assertArrayEquals(
                gsc.getDependencies().get(DEPENDENCY_FILE),
                Files.readAllBytes(Paths.get(ResourceLoader.getResource(DEPENDENCY_FILE).toURI()))
        );
    }

    /**
     * @throws IOException if an I/O error occurs
     */
    @Test
    void testWorkingDirectory() throws IOException {
        final String resource = "config/00-dependencies.yml";
        final var yaml = ResourceLoader.getResource(resource);
        Assertions.assertNotNull(yaml);
        final Loader l = this.getLoader(yaml);
        final GeneralSimulationConfig gsc = new LocalGeneralSimulationConfig(l, 0, Time.INFINITY);
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

    private Loader getLoader(final URL yaml) {
        return LoadAlchemist.from(yaml);
    }

}
