package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * Strategy to define how align the time of all trace.
 */
@FunctionalInterface
public interface GPSTimeAlignment {

    /**
     * 
     * @param traces map trace with time to align 
     * @return map trace with aligned time
     */
    ImmutableList<GPSTrace> alignTime(List<GPSTrace> traces);

}
