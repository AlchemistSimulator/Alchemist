package it.unibo.alchemist.boundary.gpsload.impl;

import java.util.Comparator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.boundary.gpsload.api.GPSTimeNormalizer;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeTimeWithFirstOfAll implements GPSTimeNormalizer {

    @Override
    public TIntObjectMap<GPSTrace> normalizeTime(final TIntObjectMap<GPSTrace> mapTrace) {
        final TIntObjectMap<GPSTrace> newMapTrace = new TIntObjectHashMap<>(mapTrace.size());
        final Time min = computeMinTime(mapTrace);
        mapTrace.forEachEntry((id, trace) -> {
            newMapTrace.put(id, trace.startAt(min));
            return true;
        });
        return newMapTrace;
    }

    private static Time computeMinTime(final TIntObjectMap<GPSTrace> mapTrace) {
        return mapTrace.valueCollection().stream()
                .map(GPSTrace::getStartTime)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("The trace can't be empty"));
    }

}
