/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.simulation;

import java.util.List;
import java.util.Objects;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;

/**
 * {@link SimulationSet} implementation.
 *
 */
public final class SimulationSetImpl implements SimulationSet {

    private static final float DEFAULT_RAM = 0;
    private static final float DEFAULT_CPU = 0;

    private final GeneralSimulationConfig genSimConfig;
    private final List<SimulationConfig> simulationConfigs;

    /**
     * 
     * @param genSimConfig Config's shared by all simulations of set
     * @param simulationConfigs List of configs that differentiate set's simulations
     */
    public SimulationSetImpl(final GeneralSimulationConfig genSimConfig,
            final List<SimulationConfig> simulationConfigs) {
        this.genSimConfig = Objects.requireNonNull(genSimConfig);
        this.simulationConfigs = Objects.requireNonNull(simulationConfigs);
    }

    @Override
    public Complexity computeComplexity() {
        return new ComplexityImpl(DEFAULT_RAM, DEFAULT_CPU);
    }

    @Override
    public GeneralSimulationConfig getGeneralSimulationConfig() {
        return this.genSimConfig;
    }

    @Override
    public List<SimulationConfig> getSimulationConfigs() {
        return this.simulationConfigs;
    }

}
