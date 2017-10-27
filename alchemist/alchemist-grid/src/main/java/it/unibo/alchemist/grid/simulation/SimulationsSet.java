package it.unibo.alchemist.grid.simulation;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;

import java.util.Set;

import it.unibo.alchemist.grid.config.SimulationConfig;

public interface SimulationsSet {
    public Complexity computeComplexity();
    public GeneralSimulationConfig getGeneralSimulationConfig();
    public Set<SimulationConfig> getSimulationConfigs();
}
