/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gps;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.maps.GPSTrace;

/**
 * Strategy to define how align the time of all trace.
 */
@FunctionalInterface
public interface GPSTimeAlignment {

    /**
     * 
     * @param traces map trace with time to align 
     * @return map trace with aligned time
     */
    ImmutableList<GPSTrace> alignTime(List<GPSTrace> traces);

}
