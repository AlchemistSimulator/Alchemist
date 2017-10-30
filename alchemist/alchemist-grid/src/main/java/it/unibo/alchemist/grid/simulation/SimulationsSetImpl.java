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
        // TODO vera complessità
        return new ComplexityImpl(0.3, 0);
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
