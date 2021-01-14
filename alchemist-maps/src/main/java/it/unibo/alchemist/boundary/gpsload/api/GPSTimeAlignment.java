/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;

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
