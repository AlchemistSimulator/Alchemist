/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;

/**
 *
 */
public class TestOSMEnvironment {

    /**
     * Tests for parallel creation of {@link OSMEnvironment}.
     * 
     * @throws Throwable if any exception occurs, it gets re-thrown, making the test fail.
     */
    @Test
    public void testConcurrentInit() throws Throwable {
        final ExecutorService executor = Executors.newFixedThreadPool(100);
        final Collection<Future<Object>> futureResults = IntStream.range(0, 100)
            .<Callable<Object>>mapToObj(i -> () -> {
                try {
                    new OSMEnvironment<>("maps/cesena.pbf");
                    return true;
                } catch (IOException e) {
                    return e;
                }
            })
            .map(executor::submit)
            .collect(Collectors.toList());
        executor.shutdown();
        for (final Future<Object> result: futureResults) {
            final Object actualResult = result.get();
            if (actualResult instanceof Throwable) {
                throw (Throwable) actualResult;
            }
        }
    }

}
