package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public abstract class AbstractGPSTimeAlignment implements GPSTimeAlignment {

    @Override
    public ImmutableList<GPSTrace> alignTime(final List<GPSTrace> traces) {
        return traces.stream()
                .map(trace -> trace.startAt(computeStartTime(traces, trace)))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * 
     * @param allTraces all {@link GPSTrace} to normalize
     * @param currentTrace current {@link GPSTrace} to normalize
     * @return the time from which the trace should begin
     */
    protected abstract Time computeStartTime(List<GPSTrace> allTraces, GPSTrace currentTrace);

}
