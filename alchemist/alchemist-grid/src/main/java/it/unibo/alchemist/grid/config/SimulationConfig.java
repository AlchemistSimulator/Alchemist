package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.Map;

/**
 * Simulation's configs for only one simulation.
 *
 */
public interface SimulationConfig extends Serializable {
    /**
     * 
     * @return Simulation's inizialization variables
     */
    Map<String, ? extends Serializable> getVariables();
}
