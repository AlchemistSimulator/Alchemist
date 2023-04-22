/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import com.google.common.collect.ImmutableList;
import it.unibo.alchemist.boundary.gpsload.api.AlignToFirstTrace;
import it.unibo.alchemist.boundary.gpsload.api.GPSTimeAlignment;
import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import org.jooq.lambda.function.Consumer3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestGPSLoader {

    private static final String BASE_DIR = "trace/";
    private static final String BASE_OK_TEST = BASE_DIR + "ok/";
    private static final String BASE_NOT_OK_TEST = BASE_DIR + "no_ok/";
    private static final String DIRECTORY_WITH_FILES = BASE_OK_TEST + "sub1/";
    private static final String DIRECTORY_WITH_SUBDIRECTORIES = BASE_OK_TEST;
    /*
     * Test file with wrong extension
     */
    private static final String WRONG_EXTENSION = BASE_NOT_OK_TEST + "wrong_extension";
    /*
     * Test file with unrecognized extension
     */
    private static final String UNRECOGNIZED_EXTENSION = BASE_NOT_OK_TEST + "unrecognized_extension";
    /*
     * Test track without segment.
     */
    private static final String NO_SEGMENTS = BASE_NOT_OK_TEST + "no_segments";
    /*
     * Test track with empty segment.
     */
    private static final String EMPTY_SEGMENT = BASE_NOT_OK_TEST + "empty_segments";
    /*
     * Test track with any point without time.
     */
    private static final String POINT_WITHOUT_TIME = BASE_NOT_OK_TEST + "point_without_time";
    private static final String CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE = "AlignToFirstTrace";
    private static final String CLASS_TIME_ALIGNMENT_TO_SIMULATION_TIME = "AlignToSimulationTime";
    private static final String CLASS_TIME_ALIGNMENT_TO_TIME = "AlignToTime";
    private static final String CLASS_TIME_NO_ALIGNMENT = "NoAlignment";
    private static final Set<String> CLASS_ALIGNMENT_NO_ARG = new HashSet<>();
    private static final GPSTimeAlignment ALIGNMENT = new AlignToFirstTrace();
    private static final List<String> ERRORS =
            ImmutableList.of(NO_SEGMENTS, EMPTY_SEGMENT, POINT_WITHOUT_TIME, WRONG_EXTENSION, UNRECOGNIZED_EXTENSION);
    private static final int NUM_MAX_TRACES = 6;
    private static final int TOTAL_POINTS = 12_196;

    /**
     * 
     */
    @BeforeEach
    public void setUp() {
        CLASS_ALIGNMENT_NO_ARG.add(CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE);
        CLASS_ALIGNMENT_NO_ARG.add(CLASS_TIME_ALIGNMENT_TO_SIMULATION_TIME);
        CLASS_ALIGNMENT_NO_ARG.add(CLASS_TIME_NO_ALIGNMENT);
    }

    /**
     * Tests traces alignment.
     * @throws IOException causes failure
     */
    @Test
    void testAlignment() throws IOException {
        for (final String normalizer : CLASS_ALIGNMENT_NO_ARG) {
            assertEquals(3, new TraceLoader(DIRECTORY_WITH_FILES, normalizer).size().orElseThrow(unexpectedCyclicTrace()));
        }
        assertEquals(3, new TraceLoader(DIRECTORY_WITH_FILES, CLASS_TIME_ALIGNMENT_TO_TIME, 0.0, false, false)
            .size().orElseThrow(unexpectedCyclicTrace()));
        final TraceLoader loaderGpx = new TraceLoader(DIRECTORY_WITH_SUBDIRECTORIES, true,  ALIGNMENT);
        final Iterator<GPSTrace> trace2 = loaderGpx.iterator();
        int points = 0;
        for (int i = 0; i < NUM_MAX_TRACES; i++) {
            if (trace2.hasNext()) {
                points += trace2.next().size();
            } else {
                fail("not loading all trace");
            }
        }
        assertTrue(trace2.hasNext());
        assertEquals(points, TOTAL_POINTS);
    }

    /**
     * Tests error reporting.
     * @throws IOException causes failure
     */
    @Test
    void testError() throws IOException {
        for (final String error : ERRORS) {
            try {
                fail("Expected error during object creation for " + error + ", got: "
                        + new TraceLoader(error, CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE));
            } catch (final IllegalStateException e) {
                /*
                 * Expected IllegalArgumentException only for UNRECOGNIZED_EXTENSION
                 */
                check(error, e.getMessage(), Assertions::assertNotEquals);
            } catch (final IllegalArgumentException e) {
                check(error, e.getMessage(), Assertions::assertEquals);
            }
        }
    }

    private static Supplier<IllegalStateException> unexpectedCyclicTrace() {
        return () -> new IllegalStateException("Unexpected cyclic trace");
    }

    private static void check(final String error, final String message, final Consumer3<String, String, String> assertion) {
        assertNotNull(message);
        assertFalse(message.isEmpty());
        assertion.accept(error, UNRECOGNIZED_EXTENSION, "Wrong exception type for " + error);
    }
}
