/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A series of tests checking that our Yaml Loader is working as expected.
 */
public class TestInSimulator {

    /**
     * Basic loading capabilities.
     */
    @Test
    public void testBase() {
        testNoVar("testbase.yml");
    }

    /**
     * Tests loading custom nodes.
     */
    @Test
    public void testCustomNodes() {
        testNoVar("customnodes.yml");
    }

    /**
     * Test the ability to load a Protelis module from classpath.
     */
    @Test
    public void testLoadProtelisModule() {
        testNoVar("test00.yml");
    }

    /**
     * Test the ability to load Protelis modules that are dynamically added and removed from classpath in a multithreaded system.
     *
     * @throws IOException causes failure
     * @throws URISyntaxException causes failure
     * @throws ExecutionException causes failure
     * @throws InterruptedException causes failure
     */
    @Test
    public void testThreadDependentLoadModule() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        final Callable<Void> c = () -> {
            testNoVar("18-export.yml");
            return null;
        };
        final File d1 = createDependenciesDirectory();
        try {
            final FutureTask<Void> ft1 = new FutureTask<>(c);
            final Thread t1 = new Thread(ft1);
            t1.setContextClassLoader(new URLClassLoader(new URL[] {d1.toURI().toURL()}));
            t1.start();
            ft1.get();
        } finally {
            FileUtils.deleteDirectory(d1);
        }
        final File d2 = createDependenciesDirectory();
        try {
            final FutureTask<Void> ft2 = new FutureTask<>(c);
            final Thread t2 = new Thread(ft2);
            t2.setContextClassLoader(new URLClassLoader(new URL[] {d2.toURI().toURL()}));
            t2.start();
            ft2.get();
        } finally {
            FileUtils.deleteDirectory(d2);
        }
    }

    private File createDependenciesDirectory() throws IOException, URISyntaxException {
        final File d = Files.createTempDir();
        FileUtils.copyDirectory(new File(ResourceLoader.getResource("advancedorig").toURI()), new File(d, "advanced"));
        FileUtils.copyDirectory(new File(ResourceLoader.getResource("plutoorig").toURI()), new File(d, "pluto"));
        return d;
    }

    /**
     * Test the ability to inject variables.
     */
    @Test
    public void testLoadWIthVariable() {
        final Map<String, Double> map = Maps.newLinkedHashMap();
        map.put("testVar", 10d);
        testLoading("test00.yml", map);
    }

    private static void testNoVar(final String resource) {
        testLoading(resource, Collections.emptyMap());
    }

    private static <T, P extends Position<P>> void testLoading(final String resource, final Map<String, Double> vars) {
        final InputStream res = ResourceLoader.getResourceAsStream(resource);
        assertNotNull(res, "Missing test resource " + resource);
        final Environment<T, P> env = new YamlLoader(res).getWith(vars);
        final Simulation<T, P> sim = new Engine<>(env, 10000);
        sim.play();
//        if (!java.awt.GraphicsEnvironment.isHeadless()) {
//            it.unibo.alchemist.boundary.gui.SingleRunGUI.make(sim);
//        }
        sim.run();
        sim.getError().ifPresent(Unchecked.consumer(e ->  {
            throw e;
        }));
    }

}
