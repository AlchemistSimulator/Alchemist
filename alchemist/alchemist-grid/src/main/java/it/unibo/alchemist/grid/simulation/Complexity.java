package it.unibo.alchemist.grid.simulation;

/**
 * An entity which represents a simulation's complexity.
 *
 */
public interface Complexity {
    /**
     * 
     * @return Simulation's ram usage in GB
     */
    double getRamUsage();
    /**
     * 
     * @return Simulation's cpu usage in %
     */
    double getCpuUsage();
}
