package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeWithTime implements GPSTimeNormalizer {

    private final Time time;

    /**
     * 
     * @param time
     *            time for normalize all trace
     */
    public NormalizeWithTime(final Time time) {
        this.time = Objects.requireNonNull(time);
    }


    @Override
    public ImmutableList<GPSTrace> normalizeTime(final List<GPSTrace> traces) {
        return traces.stream()
                .map(trace -> trace.startAt(time))
                .collect(ImmutableList.toImmutableList());
    }

}
