package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.Map;

public interface SimulationConfig extends Serializable{
    public Map<String, ? extends Serializable> getVariables();
}
