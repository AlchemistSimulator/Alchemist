package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.Map;

import it.unibo.alchemist.loader.variables.Variable;

public interface SimulationConfig {
    public Map<String, ? extends Serializable> getVariables();
}
