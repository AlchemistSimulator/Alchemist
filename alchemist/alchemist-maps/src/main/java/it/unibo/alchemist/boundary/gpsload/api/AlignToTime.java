package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;
import java.util.Objects;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Alings the traces with the given time in seconds from Epoch.
 * All points before such time will be discarded. 
 * All points after the provided time will be shifted back. 
 * Summarizing, the time that is provided represents in
 * the real world the time zero of the simulation.
 */
public class AlignToTime extends AbstractGPSTimeAlignment {

    private final Time time;

    /**
     * 
     * @param time
     *            the time from which the traces should begin. E.g., if you want all
     *            traces to begin at 2017-08-01 at 14:45:42 GMT, you should enter
     *            1501598742 (seconds from Epoch). All points before such
     *            time will be discarded. All points after the provided time will be
     *            shifted back. Summarizing, the time that is provided represents in
     *            the real world the time zero of the simulation.
     * @param exceptionForEmpty
     *            if true throw exception for empty traces, otherwise discard them
     */
    public AlignToTime(final Time time, final boolean filterEmpty, final boolean exceptionForEmpty) {
        super(getPolicy(filterEmpty, exceptionForEmpty));
        if (Objects.requireNonNull(time).toDouble() < 0) {
            throw new IllegalArgumentException("the time can't be negative");
        }
        this.time = time;
    }

    private static SinglePointBehavior getPolicy(final boolean filterEmpty, final boolean exceptionForEmpty) {
        if (filterEmpty && exceptionForEmpty) {
            return SinglePointBehavior.THROW_EXCEPTION_ON_SINGLE_POINTS;
        }
        if (filterEmpty) {
            return SinglePointBehavior.DISCARD_SINGLE_POINTS;
        }
        if (!filterEmpty && !exceptionForEmpty) {
            return SinglePointBehavior.RETAIN_SINGLE_POINTS;
        }
        throw new IllegalArgumentException("Invalid combination of parameter filterEmpty: " + filterEmpty
                + " exceptionForEmpty: " + exceptionForEmpty);
    }

    /**
     * 
     * @param time
     *            the time from which the traces should begin. E.g., if you want all
     *            traces to begin at 2017-08-01 at 14:45:42 GMT, you should enter
     *            1501598742 (seconds from Epoch). All points before such
     *            time will be discarded. All points after the provided time will be
     *            shifted back. Summarizing, the time that is provided represents in
     *            the real world the time zero of the simulation.
     * @param exceptionForEmpty
     *            if true throw exception for empty traces, otherwise discard them
     */
    public AlignToTime(final double time, final boolean filterEmpty, final boolean exceptionForEmpty) {
        this(new DoubleTime(time), filterEmpty, exceptionForEmpty);
    }

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return time;
    }

}
