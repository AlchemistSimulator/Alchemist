package it.unibo.alchemist.boundary.gpsload.api;

import java.util.Comparator;
import java.util.List;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeTimeWithFirstOfAll extends AbstractGPSTimeNormalizer {

    private Time time;

    private void computeMinTime(final List<GPSTrace> allTraces) {
        time = allTraces.stream()
                .map(GPSTrace::getStartTime)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("The trace can't be empty"));
    }

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        if (time == null) {
            computeMinTime(allTraces);
        }
        return time;
    }

}
