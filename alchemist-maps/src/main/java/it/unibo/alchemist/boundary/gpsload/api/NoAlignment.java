/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gpsload.api;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.Time;

import java.util.List;

/**
 * No alignment is performed.
 * If you have two traces, the first trace start with time = 2 and second point with time = 5,
 * the second trace start with time = 4 and second point with time = 6,
 * the result will be: 
 * - first trace start with time = 2 and second point with time = 5
 * - second trace start with time = 4 and second point with time = 6
 */
public final class NoAlignment extends AbstractGPSTimeAlignment {

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
