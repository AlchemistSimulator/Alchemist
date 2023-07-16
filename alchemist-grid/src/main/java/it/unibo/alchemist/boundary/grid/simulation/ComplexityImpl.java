/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.simulation;

/**
 * {@link Complexity} implementation.
 *
 */
public final class ComplexityImpl implements Complexity {

    private final double ram;
    private final double cpu;

    /**
     * 
     * @param ram Simulation's ram usage in GB
     * @param cpu Simulation's cpu usage in %
     */
    public ComplexityImpl(final double ram, final double cpu) {
        this.ram = ram;
        this.cpu = cpu;
    }

    @Override
    public double getRamUsage() {
        return this.ram;
    }

    @Override
    public double getCpuUsage() {
        return this.cpu;
    }

}
