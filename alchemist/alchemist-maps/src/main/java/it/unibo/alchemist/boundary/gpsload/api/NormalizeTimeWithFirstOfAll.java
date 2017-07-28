package it.unibo.alchemist.boundary.gpsload.impl;

import java.util.Comparator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import it.unibo.alchemist.boundary.gpsload.api.GPSTimeNormalizer;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeTimeWithFirstOfAll implements GPSTimeNormalizer {

    private static Time computeMinTime(final List<GPSTrace> trace) {
        return trace.stream()
                .map(GPSTrace::getStartTime)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("The trace can't be empty"));
    }

    @Override
    public ImmutableList<GPSTrace> normalizeTime(final List<GPSTrace> traces) {
        final Time min = computeMinTime(traces);
        return traces.stream()
                .map(trace -> trace.startAt(min))
                .collect(ImmutableList.toImmutableList());
    }

}
