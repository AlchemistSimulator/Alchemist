/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.model.SupportedIncarnations;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Incarnation;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
class TestOSMEnvironment {

    /**
     * Tests for parallel creation of {@link OSMEnvironment}.
     * 
     * @throws Throwable if any exception occurs, it gets re-thrown, making the test fail.
     */
    @Test
    void testConcurrentInit() throws Throwable {
        final ExecutorService executor = Executors.newFixedThreadPool(100);
        final Incarnation<Object, GeoPosition> incarnation =
                SupportedIncarnations.<Object, GeoPosition>get("protelis").orElseThrow();
        final Collection<Future<Object>> futureResults = IntStream.range(0, 100)
            .<Callable<Object>>mapToObj(i -> () -> {
                new OSMEnvironment<>(incarnation, "maps/cesena.pbf");
                return true;
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
