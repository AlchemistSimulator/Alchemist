/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.alignments;

import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.maps.GPSTrace;
import it.unibo.alchemist.model.times.DoubleTime;

import java.util.List;
import java.util.Objects;

/**
 * Aligns the traces with the given time in seconds from Epoch. All points
 * before such time will be discarded. All points after the provided time will
 * be shifted back. Summarizing, the time that is provided represents in the
 * real world the time zero of the simulation.
 */
public final class AlignToTime extends AbstractGPSTimeAlignment {

    private final Time time;

    /**
     * @param time
     *            the time from which the traces should begin. E.g., if you want all
     *            traces to begin at 2017-08-01 at 14:45:42 GMT, you should enter
     *            1,501,598,742 (seconds from Epoch). All points before such time will
     *            be discarded. All points after the provided time will be shifted
     *            back. Summarizing, the time that is provided represents in the
     *            real world the time zero of the simulation.
     * @param filterEmpty
     *            if true, filter empty traces
     * @param exceptionForEmpty
     *            if true, throws exception for empty traces
     */
    public AlignToTime(final double time, final boolean filterEmpty, final boolean exceptionForEmpty) {
        this(new DoubleTime(time), filterEmpty, exceptionForEmpty);
    }

    /**
     * @param time
     *            the time from which the traces should begin. E.g., if you want all
     *            traces to begin at 2017-08-01 at 14:45:42 GMT, you should enter
     *            1,501,598,742 (seconds from Epoch). All points before such time will
     *            be discarded. All points after the provided time will be shifted
     *            back. Summarizing, the time that is provided represents in the
     *            real world the time zero of the simulation.
     * @param filterEmpty
     *            if true, filter empty traces
     * @param exceptionForEmpty
     *            if true, throws exception for empty traces
     */
    public AlignToTime(final Time time, final boolean filterEmpty, final boolean exceptionForEmpty) {
        super(getPolicy(filterEmpty, exceptionForEmpty));
        if (Objects.requireNonNull(time).toDouble() < 0) {
            throw new IllegalArgumentException("the time can't be negative");
        }
        this.time = time;
    }

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        return time;
    }

    private static SinglePointBehavior getPolicy(final boolean filterEmpty, final boolean exceptionForEmpty) {
        if (filterEmpty && exceptionForEmpty) {
            return SinglePointBehavior.THROW_EXCEPTION_ON_SINGLE_POINTS;
        }
        if (filterEmpty) {
            return SinglePointBehavior.DISCARD_SINGLE_POINTS;
        }
        /*
         * if you get here, filterEmpty is false.
         */
        if (!exceptionForEmpty) {
            return SinglePointBehavior.RETAIN_SINGLE_POINTS;
        }
        throw new IllegalArgumentException("Invalid combination of parameter filterEmpty and exceptionForEmpty");
    }

}
