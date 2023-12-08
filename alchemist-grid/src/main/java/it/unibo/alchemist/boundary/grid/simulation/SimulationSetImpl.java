/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.simulation;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.boundary.grid.config.SimulationConfig;

import javax.annotation.Nonnull;

/**
 * {@link SimulationSet} implementation.
 *
 */
public final class SimulationSetImpl implements SimulationSet {

    private static final float DEFAULT_RAM = 0;
    private static final float DEFAULT_CPU = 0;

    private final GeneralSimulationConfig genSimConfig;
    private final ImmutableList<SimulationConfig> simulationConfigs;

    /**
     * 
     * @param genSimConfig Config's shared by all simulations of set
     * @param simulationConfigs List of configs that differentiate set's simulations
     */
    public SimulationSetImpl(
        @Nonnull final GeneralSimulationConfig genSimConfig,
        @Nonnull final List<SimulationConfig> simulationConfigs
    ) {
        this.genSimConfig = Objects.requireNonNull(genSimConfig);
        this.simulationConfigs = ImmutableList.copyOf(simulationConfigs);
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
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "The field is immutable")
    public ImmutableList<SimulationConfig> getSimulationConfigs() {
        return this.simulationConfigs;
    }

}
