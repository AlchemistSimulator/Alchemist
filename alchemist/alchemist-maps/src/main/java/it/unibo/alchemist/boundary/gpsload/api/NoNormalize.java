package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * no normalize time, return the identical list of {@link GPSTrace} but immutable.
 */
public class NoNormalize extends AbstractGPSTimeNormalizer {

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return currentTrace.getStartTime();
    }
}
