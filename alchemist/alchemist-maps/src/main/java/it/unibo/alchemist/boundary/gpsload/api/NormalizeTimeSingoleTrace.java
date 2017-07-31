package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class NormalizeTimeSingoleTrace implements GPSTimeNormalizer {

    @Override
    public ImmutableList<GPSTrace> normalizeTime(final List<GPSTrace> traces) {
        return traces.stream()
                .map(trace -> trace.startAt(trace.getStartTime()))
                .collect(ImmutableList.toImmutableList());
    }

}
