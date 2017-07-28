package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * Strategy to define how normalize the time of all trace.
 */
@FunctionalInterface
public interface GPSTimeNormalizer {

    /**
     * 
     * @param traces map trace with time to normalize 
     * @return map trace with normalized time
     */
    ImmutableList<GPSTrace> normalizeTime(List<GPSTrace> traces);

}
