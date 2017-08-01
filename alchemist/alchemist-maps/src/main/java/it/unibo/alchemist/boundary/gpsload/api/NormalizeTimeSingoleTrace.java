package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeTimeSingoleTrace extends AbstractGPSTimeNormalizer {

    @Override
    public ImmutableList<GPSTrace> normalizeTime(final List<GPSTrace> traces) {
        return traces.stream()
                .map(trace -> trace.startAt(trace.getStartTime()))
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return currentTrace.getStartTime();
    }

}
