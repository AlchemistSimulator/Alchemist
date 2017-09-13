package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Aligns all traces at the initial simulation time.
 * If you have two traces, the first trace start with time = 2 and second point with time = 5,
 * the second trace start with time = 4 and second point with time = 6,
 * the result will be: 
 * - first trace start with time = 0 and second point with time = 3
 * - second trace start with time = 0 and second point with time = 2
 */
public class AlignToSimulationTime extends AbstractGPSTimeAlignment {

    private static final SinglePointBehavior POLICY = SinglePointBehavior.RETAIN_SINGLE_POINTS;

    /**
     * Default empty constructor, builds a AlignToSimulationTime with RETAIN_SINGLE_POINTS
     * behavior for trace with single point.
     */
    public AlignToSimulationTime() {
        super(POLICY);
    }

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return currentTrace.getStartTime();
    }

}
