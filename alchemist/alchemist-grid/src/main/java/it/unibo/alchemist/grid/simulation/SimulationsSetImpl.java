package it.unibo.alchemist.grid.simulation;

import java.util.List;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;

public class SimulationsSetImpl<T> implements SimulationsSet<T> {

    private final GeneralSimulationConfig<T> genSimConfig;
    private final List<SimulationConfig> simulationConfigs;

    public SimulationsSetImpl(GeneralSimulationConfig<T> genSimConfig, List<SimulationConfig> simulationConfigs) {
        super();
        this.genSimConfig = genSimConfig;
        this.simulationConfigs = simulationConfigs;
    }

    @Override
    public Complexity computeComplexity() {
        // TODO vera complessità
        return new ComplexityImpl(1, 0);
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
