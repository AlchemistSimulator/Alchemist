package it.unibo.alchemist.boundary.gpsload.api;

import java.util.Comparator;
import java.util.List;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 * Aligns all traces at the start time of the first trace.
 * If you have two traces, the first trace start with time = 2 and second point with time = 5,
 * the second trace start with time = 4 and second point with time = 6,
 * the result will be: 
 * - first trace start with time = 0 and second point with time = 3
 * - second trace start with time = 2 and second point with time = 4
 */
public class AlignToFirstTrace extends AbstractGPSTimeAlignment {

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
