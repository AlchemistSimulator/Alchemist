/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
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
public final class AlignToSimulationTime extends AbstractGPSTimeAlignment {

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
