package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeTimeSingleTrace extends AbstractGPSTimeNormalizer {

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return currentTrace.getStartTime();
    }

}
