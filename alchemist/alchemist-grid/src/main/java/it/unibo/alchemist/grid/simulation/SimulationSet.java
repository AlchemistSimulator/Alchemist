package it.unibo.alchemist.grid.simulation;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;

import java.util.List;

import it.unibo.alchemist.grid.config.SimulationConfig;

/**
 * Set of configs for remote simulations creation.
 *
 * @param <T>
 */
public interface SimulationSet<T> {
    /**
     * 
     * @return complexity of one simulation
     */
    Complexity computeComplexity();
    /**
     * 
     * @return Config's shared by all simulations of set
     */
    GeneralSimulationConfig<?> getGeneralSimulationConfig();
    /**
     * 
     * @return List of configs that differentiate set's simulations
     */
    List<SimulationConfig> getSimulationConfigs();
}
