/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.simulation;

/**
 * An entity which represents a simulation's complexity.
 *
 */
public interface Complexity {
    /**
     * 
     * @return Simulation's ram usage in GB
     */
    double getRamUsage();
    /**
     * 
     * @return Simulation's cpu usage in %
     */
    double getCpuUsage();
}
