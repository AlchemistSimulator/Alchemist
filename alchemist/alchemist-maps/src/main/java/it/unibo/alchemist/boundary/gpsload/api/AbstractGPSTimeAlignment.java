package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public abstract class AbstractGPSTimeAlignment implements GPSTimeAlignment {

    private final SinglePointBehavior policy;
    /**
     * 
     */
    protected enum SinglePointBehavior {
        /**
         * 
         */
        RETAIN_SINGLE_POINTS,
        /**
         * 
         */
        DISCARD_SINGLE_POINTS,
        /**
         * 
         */
        THROW_EXCEPTION_ON_SINGLE_POINTS
    }

    /**
     * 
     * @param policy define policy for trace with single point
     */
    protected AbstractGPSTimeAlignment(final SinglePointBehavior policy) {
        this.policy = policy;
    }

    @Override
    public ImmutableList<GPSTrace> alignTime(final List<GPSTrace> traces) {
        Stream<GPSTrace> stream = traces.stream().map(trace -> trace.startAt(computeStartTime(traces, trace)));
        if (policy == SinglePointBehavior.DISCARD_SINGLE_POINTS) {
            stream = stream.filter(trace -> trace.size() <= 1);
        }
        if (policy == SinglePointBehavior.THROW_EXCEPTION_ON_SINGLE_POINTS) {
            final ImmutableList<GPSTrace> reified = stream.collect(ImmutableList.toImmutableList());
            final Optional<GPSTrace> single = reified.stream().filter(t -> t.size() <= 1).findAny();
            if (single.isPresent()) {
                throw new IllegalStateException("Time alignment produced a trace with a single point: " + single.get());
            }
            stream = reified.stream();
        }
        return stream.collect(ImmutableList.toImmutableList());
    }

    /**
     * 
     * @param allTraces all {@link GPSTrace} to normalize
     * @param currentTrace current {@link GPSTrace} to normalize
     * @return the time from which the trace should begin
     */
    protected abstract Time computeStartTime(List<GPSTrace> allTraces, GPSTrace currentTrace);

}
