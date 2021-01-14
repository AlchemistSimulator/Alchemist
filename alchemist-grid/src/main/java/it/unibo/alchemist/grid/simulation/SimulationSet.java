/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.simulation;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;

import java.util.List;

import it.unibo.alchemist.grid.config.SimulationConfig;

/**
 * Set of configs for remote simulations creation.
 *
 */
public interface SimulationSet {
    /**
     * 
     * @return complexity of one simulation
     */
    Complexity computeComplexity();
    /**
     * 
     * @return Config's shared by all simulations of set
     */
    GeneralSimulationConfig getGeneralSimulationConfig();
    /**
     * 
     * @return List of configs that differentiate set's simulations
     */
    List<SimulationConfig> getSimulationConfigs();
}
