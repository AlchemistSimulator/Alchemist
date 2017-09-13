package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * No alignment is performed.
 * If you have two traces, the first trace start with time = 2 and second point with time = 5,
 * the second trace start with time = 4 and second point with time = 6,
 * the result will be: 
 * - first trace start with time = 2 and second point with time = 5
 * - second trace start with time = 4 and second point with time = 6
 */
public class NoAlignment extends AbstractGPSTimeAlignment {

    private static final SinglePointBehavior POLICY = SinglePointBehavior.RETAIN_SINGLE_POINTS;

    /**
     * Default empty constructor, builds a NoAlignment with RETAIN_SINGLE_POINTS
     * behavior for trace with single point.
     */
    public NoAlignment() {
        super(POLICY);
    }


    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return new DoubleTime(0.0);
    }
}
