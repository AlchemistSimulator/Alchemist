package it.unibo.alchemist.test;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.boundary.gpsload.api.NoAlignment;
import it.unibo.alchemist.boundary.gpsload.api.NormalizeTimeWithFirstOfAll;
import it.unibo.alchemist.boundary.gpsload.api.AlignToSimulationTime;
import it.unibo.alchemist.boundary.gpsload.api.NormalizeWithTime;
import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.routes.GPSTraceImpl;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Test time normalizer.
 */
public class TestNormalizer {

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
    private static final Double DELTA = 0.0;

    /**
     * 
     */
    @Before
    public void setUp() {
        TRACES.add(TRACE_1);
        TRACES.add(TRACE_2);
        TRACES.add(TRACE_3);
    }

    /**
     * 
     */
    @Test
    public void testNoNormalize() {
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
    public void testNormalizeTimeWithFirstOfAll() {
        final ImmutableList<GPSTrace> traces = new NormalizeTimeWithFirstOfAll().alignTime(TRACES);
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
    public void testNormalizeTimeSingleTrace() {
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
    public void testNormalizeWithTime() {
        final Time time = new DoubleTime(2.0);
        final ImmutableList<GPSTrace> traces = new NormalizeWithTime(time).alignTime(TRACES);
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
                traces.get(0).getTime(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble() - findNextTime(TRACE_2, time).toDouble(),
                traces.get(1).getTime(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - findNextTime(TRACE_3, time).toDouble(),
                traces.get(2).getTime(), DELTA);
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

    private Time findNextTime(final GPSTrace trace, final Time time) {
        return trace.getNextPosition(time).getTime();
    }

    private void testRequiredWalkTime(final ImmutableList<GPSTrace> traces) {
        /*
         * Test the time required to walk the trace
         */
        assertEquals(TRACE_1_POINT_3.getTime().toDouble() - TRACE_1_POINT_1.getTime().toDouble(),
                traces.get(0).getTime(), DELTA);
        assertEquals(TRACE_2_POINT_3.getTime().toDouble() - TRACE_2_POINT_1.getTime().toDouble(),
                traces.get(1).getTime(), DELTA);
        assertEquals(TRACE_3_POINT_3.getTime().toDouble() - TRACE_3_POINT_1.getTime().toDouble(),
                traces.get(2).getTime(), DELTA);
    }
}
