package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.routes.GPSTraceImpl;
import it.unibo.alchemist.model.implementations.times.DoubleTime;

public class TestGPSTraceImpl {

    @Test
    public void testConstructionWithList() {
        assertNotNull(new GPSTraceImpl(ImmutableList.of(
                new GPSPointImpl(0d,  0d, DoubleTime.ZERO_TIME))));
    }

}
