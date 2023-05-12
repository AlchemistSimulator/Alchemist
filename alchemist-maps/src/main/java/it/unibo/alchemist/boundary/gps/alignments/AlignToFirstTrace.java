/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gps.alignments;

import java.util.Comparator;
import java.util.List;

import it.unibo.alchemist.model.maps.GPSTrace;
import it.unibo.alchemist.model.Time;

/**
 * 
 * Aligns all traces at the start time of the first trace.
 * If you have two traces, the first trace start with time = 2 and second point with time = 5,
 * the second trace start with time = 4 and second point with time = 6,
 * the result will be: 
 * - first trace start with time = 0 and second point with time = 3
 * - second trace start with time = 2 and second point with time = 4
 */
public final class AlignToFirstTrace extends AbstractGPSTimeAlignment {

    private static final SinglePointBehavior POLICY = SinglePointBehavior.RETAIN_SINGLE_POINTS;
    private Time time;

    /**
     * Default empty constructor, builds a AlignToFirstTrace with RETAIN_SINGLE_POINTS
     * behavior for trace with single point.
     */
    public AlignToFirstTrace() {
        super(POLICY);
    }

    private void computeMinTime(final List<GPSTrace> allTraces) {
        time = allTraces.stream()
                .map(GPSTrace::getStartTime)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("The trace can't be empty"));
    }

    @Override
    protected Time computeStartTime(final List<GPSTrace> allTraces, final GPSTrace currentTrace) {
        if (time == null) {
            computeMinTime(allTraces);
        }
        return time;
    }

}
