package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;
import java.util.Objects;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeWithTime extends AbstractGPSTimeAlignment {

    private final Time time;

    /**
     * 
     * @param time
     *            the time from which the traces should begin. E.g., if you want all
     *            traces to begin at 2017-08-01 at 14:45:42 GMT, you should enter
     *            1501598742000 (milliseconds from Epoch). All points before such
     *            time will be discarded. All points after the provided time will be
     *            shifted back. Summarizing, the time that is provided represents in
     *            the real world the time zero of the simulation.
     */
    public NormalizeWithTime(final Time time) {
        if (Objects.requireNonNull(time).toDouble() < 0) {
            throw new IllegalArgumentException("the time can't be negative");
        }
        this.time = time;
    }

    /**
     * 
     * @param time
     *            the time from which the traces should begin. E.g., if you want all
     *            traces to begin at 2017-08-01 at 14:45:42 GMT, you should enter
     *            1501598742000 (milliseconds from Epoch). All points before such
     *            time will be discarded. All points after the provided time will be
     *            shifted back. Summarizing, the time that is provided represents in
     *            the real world the time zero of the simulation.
     */
    public NormalizeWithTime(final double time) {
        this(new DoubleTime(time));
    }

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return time;
    }

}
