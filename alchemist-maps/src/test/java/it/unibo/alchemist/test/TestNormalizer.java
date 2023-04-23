/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.boundary.gpsload.api.AlignToFirstTrace;
import it.unibo.alchemist.boundary.gpsload.api.AlignToSimulationTime;
import it.unibo.alchemist.boundary.gpsload.api.AlignToTime;
import it.unibo.alchemist.boundary.gpsload.api.NoAlignment;
import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.routes.GPSTraceImpl;
import it.unibo.alchemist.model.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.Time;

/**
 * Test time normalizer.
 */
class TestNormalizer {

    private static final GPSPoint TRACE_1_POINT_1 = new GPSPointImpl(1.0, 2.0, new DoubleTime(3.0));
    private static final GPSPoint TRACE_1_POINT_2 = new GPSPointImpl(1.0, 2.0, new DoubleTime(5.0));
    private static final GPSPoint TRACE_1_POINT_3 = new GPSPointImpl(1.0, 2.0, new DoubleTime(6.0));

    private static final GPSPoint TRACE_2_POINT_1 = new GPSPointImpl(1.0, 2.0, new DoubleTime(1.0));
    private static final GPSPoint TRACE_2_POINT_2 = new GPSPointImpl(1.0, 2.0, new DoubleTime(3.0));
    private static final GPSPoint TRACE_2_POINT_3 = new GPSPointImpl(1.0, 2.0, new DoubleTime(4.0));

    private static final GPSPoint TRACE_3_POINT_1 = new GPSPointImpl(1.0, 2.0, new DoubleTime(5.0));
    private static final GPSPoint TRACE_3_POINT_2 = new GPSPointImpl(1.0, 2.0, new DoubleTime(6.0));
    private static final GPSPoint TRACE_3_POINT_3 = new GPSPointImpl(1.0, 2.0, new DoubleTime(7.0));
    private static final GPSTrace TRACE_1 = new GPSTraceImpl(TRACE_1_POINT_1, TRACE_1_POINT_2, TRACE_1_POINT_3);
    private static final GPSTrace TRACE_2 = new GPSTraceImpl(TRACE_2_POINT_1, TRACE_2_POINT_2, TRACE_2_POINT_3);
    private static final GPSTrace TRACE_3 = new GPSTraceImpl(TRACE_3_POINT_1, TRACE_3_POINT_2, TRACE_3_POINT_3);
    private static final List<GPSTrace> TRACES = new LinkedList<>();
    private static final Double DELTA = Double.MIN_VALUE;

    static {
        TRACES.add(TRACE_1);
        TRACES.add(TRACE_2);
        TRACES.add(TRACE_3);
    }

    /**
     * 
     */
    @Test
    void testNoAlignment() {
        final ImmutableList<GPSTrace> traces = new NoAlignment().alignTime(TRACES);
        /*
         * Test start time
         */
        assertEquals(TRACE_1_POINT_1.getTime().toDouble(), traces.get(0).getStartTime().toDouble(), DELTA);
        assertEquals(TRACE_2_POINT_1.getTime().toDouble(), traces.get(1).getStartTime().toDouble(), DELTA);
        assertEquals(TRACE_3_POINT_1.getTime().toDouble(), traces.get(2).getStartTime().toDouble(), DELTA);

        testRequiredWalkTime(traces);

        /*
         * Test final time 
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble(), traces.get(0).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble(), traces.get(1).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble(), traces.get(2).getFinalTime().toDouble(), DELTA);
    }

    /**
     * 
     */
    @Test
    void testAlignToFirstTrace() {
        final ImmutableList<GPSTrace> traces = new AlignToFirstTrace().alignTime(TRACES);
        /*
         * Test start time
         */
        assertEquals(TRACE_1_POINT_1.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(), 
                traces.get(0).getStartTime().toDouble(), DELTA);
        assertEquals(TRACE_2_POINT_1.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(1).getStartTime().toDouble(), DELTA);
        assertEquals(TRACE_3_POINT_1.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(2).getStartTime().toDouble(), DELTA);

        testRequiredWalkTime(traces);

        /*
         * Test final time 
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(0).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(1).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(2).getFinalTime().toDouble(), DELTA);
    }

    /**
     * 
     */
    @Test
    void testAlignToSimulationTime() {
        final ImmutableList<GPSTrace> traces = new AlignToSimulationTime().alignTime(TRACES);
        /*
         * Test start time
         */
        assertEquals(0.0, traces.get(0).getStartTime().toDouble(), DELTA);
        assertEquals(0.0, traces.get(1).getStartTime().toDouble(), DELTA);
        assertEquals(0.0, traces.get(2).getStartTime().toDouble(), DELTA);

        testRequiredWalkTime(traces);

        /*
         * Test final time 
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - TRACE_1_POINT_1.getTime().toDouble(),
                traces.get(0).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(1).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - TRACE_3_POINT_1.getTime().toDouble(),
                traces.get(2).getFinalTime().toDouble(), DELTA);
    }

    /**
     * 
     */
    @Test
    void testAlignToTimeRetainSinglePoint() {
        final Time time = new DoubleTime(2.0);
        final ImmutableList<GPSTrace> traces = new AlignToTime(time, false, false).alignTime(TRACES);
        /*
         * Test start time
         */
        assertEquals(findNextTime(TRACE_1, time).toDouble() - time.toDouble(), 
                traces.get(0).getStartTime().toDouble(), DELTA);
        assertEquals(findNextTime(TRACE_2, time).toDouble() - time.toDouble(), 
                traces.get(1).getStartTime().toDouble(), DELTA);
        assertEquals(findNextTime(TRACE_3, time).toDouble() - time.toDouble(), 
                traces.get(2).getStartTime().toDouble(), DELTA);
        /*
         * Test the time required to walk the trace
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - findNextTime(TRACE_1, time).toDouble(),
                traces.get(0).getTripTime(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble() - findNextTime(TRACE_2, time).toDouble(),
                traces.get(1).getTripTime(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - findNextTime(TRACE_3, time).toDouble(),
                traces.get(2).getTripTime(), DELTA);
        /*
         * Test final time 
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - time.toDouble(),
                traces.get(0).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble() - time.toDouble(),
                traces.get(1).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - time.toDouble(),
                traces.get(2).getFinalTime().toDouble(), DELTA);
    }

    /**
     * 
     */
    @Test
    void testAlignToTimeDiscardSinglePoint() {
        final Time time = new DoubleTime(4.0);
        final ImmutableList<GPSTrace> traces = new AlignToTime(time, true, false).alignTime(TRACES);
        /*
         * Test number of trace
         */
        assertEquals(2, traces.size());
        /*
         * Test start time
         */
        assertEquals(findNextTime(TRACE_1, time).toDouble() - time.toDouble(), 
                traces.get(0).getStartTime().toDouble(), DELTA);
        assertEquals(findNextTime(TRACE_3, time).toDouble() - time.toDouble(), 
                traces.get(1).getStartTime().toDouble(), DELTA);

        /*
         * Test the time required to walk the trace
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - findNextTime(TRACE_1, time).toDouble(),
                traces.get(0).getTripTime(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - findNextTime(TRACE_3, time).toDouble(),
                traces.get(1).getTripTime(), DELTA);
        /*
         * Test final time 
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - time.toDouble(),
                traces.get(0).getFinalTime().toDouble(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - time.toDouble(),
                traces.get(1).getFinalTime().toDouble(), DELTA);
    }

    /**
     * 
     */
    @Test
    void testAlignToTimeThrowExceptionOnSinglePoint() {
        final Time time = new DoubleTime(4.0);
        try {
            new AlignToTime(time, true, true).alignTime(TRACES);
            fail("not throw exception");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * 
     */
    @Test
    void testAlignToTimeNegativeTime() {
        final Time time = new DoubleTime(-4.0);
        try {
            new AlignToTime(time, true, true);
            fail("not throw exception");
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * 
     */
    @Test
    void testAlignToTimeWrongPolicy() {
        final Time time = new DoubleTime(4.0);
        try {
            new AlignToTime(time, false, true).alignTime(TRACES);
            fail("not throw exception");
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    private Time findNextTime(final GPSTrace trace, final Time time) {
        return trace.getNextPosition(time).getTime();
    }

    private void testRequiredWalkTime(final ImmutableList<GPSTrace> traces) {
        /*
         * Test the time required to walk the trace
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - TRACE_1_POINT_1.getTime().toDouble(),
                traces.get(0).getTripTime(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(1).getTripTime(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - TRACE_3_POINT_1.getTime().toDouble(),
                traces.get(2).getTripTime(), DELTA);
    }
}
