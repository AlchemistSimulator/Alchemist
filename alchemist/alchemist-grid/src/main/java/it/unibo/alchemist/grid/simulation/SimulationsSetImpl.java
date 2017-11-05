package it.unibo.alchemist.grid.simulation;

import java.util.List;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;

/**
 * {@link SimulationsSet} implementation.
 *
 * @param <T>
 */
public class SimulationsSetImpl<T> implements SimulationsSet<T> {

    private static final float DEFAULT_RAM = 0;
    private static final float DEFAULT_CPU = 0;

    private final GeneralSimulationConfig<T> genSimConfig;
    private final List<SimulationConfig> simulationConfigs;

    /**
     * 
     * @param genSimConfig Config's shared by all simulations of set
     * @param simulationConfigs List of configs that differentiate set's simulations
     */
    public SimulationsSetImpl(final GeneralSimulationConfig<T> genSimConfig, final List<SimulationConfig> simulationConfigs) {
        super();
        this.genSimConfig = genSimConfig;
        this.simulationConfigs = simulationConfigs;
    }

    @Override
    public Complexity computeComplexity() {
        return new ComplexityImpl(DEFAULT_RAM, DEFAULT_CPU);
    }

    @Override
    public GeneralSimulationConfig<?> getGeneralSimulationConfig() {
        return this.genSimConfig;
    }

    @Override
    public List<SimulationConfig> getSimulationConfigs() {
        return this.simulationConfigs;
    }

}
