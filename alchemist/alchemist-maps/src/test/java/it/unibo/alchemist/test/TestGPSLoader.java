/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unibo.alchemist.boundary.gpsload.api.GPSTimeAlignment;
import it.unibo.alchemist.boundary.gpsload.api.AlignToFirstTrace;
import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class TestGPSLoader {

    private static final String BASE_DIR = "trace/";
    private static final String BASE_OK_TEST = BASE_DIR + "ok/";
    private static final String BASE_NOT_OK_TEST = BASE_DIR + "no_ok/";
    private static final String DIRECTORY_WITH_FILES = BASE_OK_TEST + "sub1/";
    private static final String DIRECTORY_WITH_SUBDIRECTORIES = BASE_OK_TEST;
    private static final String WRONG_EXTENSION = BASE_NOT_OK_TEST + "wrong_extension/";
    private static final String UNRECOGNIZED_EXTENSION = BASE_NOT_OK_TEST + "unrecognized_extension/";
    private static final String NO_SEGMENTS = BASE_NOT_OK_TEST + "no_segments/";
    private static final String EMPTY_SEGMENT = BASE_NOT_OK_TEST + "empty_segments/";
    private static final String POINT_WITHOUT_TIME = BASE_NOT_OK_TEST + "point_without_time/";
    private static final String CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE = "AlignToFirstTrace";
    private static final String CLASS_TIME_ALIGNMENT_TO_SIMULATION_TIME = "AlignToSimulationTime";
    private static final String CLASS_TIME_ALIGNMENT_TO_TIME = "AlignToTime";
    private static final String CLASS_TIME_NO_ALIGNMENT = "NoAlignment";
    private static final Set<String> CLASS_ALIGNMENT_NO_ARG = new HashSet<>();
    private static final GPSTimeAlignment ALIGNMENT = new AlignToFirstTrace();

    private TraceLoader loaderGpx;
    private static final int NUM_MAX_TRACES = 6;
    private static final int TOTAL_POINTS = 12196; 

    /**
     * 
     */
    @Before
    public void setUp() {
        CLASS_ALIGNMENT_NO_ARG.add(CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE);
        CLASS_ALIGNMENT_NO_ARG.add(CLASS_TIME_ALIGNMENT_TO_SIMULATION_TIME);
        CLASS_ALIGNMENT_NO_ARG.add(CLASS_TIME_NO_ALIGNMENT);
    }
    /**
     * TODO: review test code, its quality is currently too low
     */
    @Test
    public void testOk() {
        for (final String normalizer : CLASS_ALIGNMENT_NO_ARG) {
            try {
                this.loaderGpx = new TraceLoader(DIRECTORY_WITH_FILES, normalizer);
                final Iterator<GPSTrace> trace = this.loaderGpx.iterator();
                for (int i = 0; i < 3; i++) {
                    if (trace.hasNext()) {
                        trace.next();
                    } else {
                        fail("not loading all trace");
                    }
                }
                assertFalse("Load more traces than expected", trace.hasNext());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_FILES, CLASS_TIME_ALIGNMENT_TO_TIME, 0.0, false, false);
            final Iterator<GPSTrace> trace = this.loaderGpx.iterator();
            for (int i = 0; i < 3; i++) {
                if (trace.hasNext()) {
                    trace.next();
                } else {
                    fail("not loading all trace");
                }
            }
            assertFalse("Load more traces of the due", trace.hasNext());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_SUBDIRECTORIES, true,  ALIGNMENT);
            final Iterator<GPSTrace> trace = this.loaderGpx.iterator();
            int points = 0;
            for (int i = 0; i < NUM_MAX_TRACES; i++) {
                if (trace.hasNext()) {
                    points += trace.next().size();
                } else {
                    fail("not loading all trace");
                }
            }
            assertTrue("the iterator does not cycle", trace.hasNext());
            assertEquals(points, TOTAL_POINTS);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * 
     */
    @Test
    public void testError() {

        /*
         * Test track without segment.
         */
        try {
            this.loaderGpx = new TraceLoader(NO_SEGMENTS, CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE);
            fail("not exception for no segments");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        /*
         * Test track with empty segment.
         */
        try {
            this.loaderGpx = new TraceLoader(EMPTY_SEGMENT, CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE);
            fail("not exception for empty segment");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        /*
         * Test track with any point without time.
         */
        try {
            this.loaderGpx = new TraceLoader(POINT_WITHOUT_TIME, CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE);
            fail("not exception for point without time");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        /*
         * Test file with wrong extension
         */
        try {
            this.loaderGpx = new TraceLoader(WRONG_EXTENSION, CLASS_TIME_ALIGNMENT_TO_FIRST_TRACE);
            fail("not exception for wrong extension");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        } 

        /*
         * Test file with unrecognized extension
         */
        try {
            this.loaderGpx = new TraceLoader(UNRECOGNIZED_EXTENSION, ALIGNMENT);
            fail("not exception for unrecognized extension");
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
