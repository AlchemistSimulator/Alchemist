package it.unibo.alchemist.grid.config;

import java.util.Map;

import it.unibo.alchemist.loader.variables.Variable;

public interface SimulationConfig {
    public Map<String, Variable<?>> getVariables();
}
