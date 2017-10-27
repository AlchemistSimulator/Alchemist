package it.unibo.alchemist.grid.simulation;

import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;

public class RemoteSimulationImpl implements RemoteSimulation {

    private final GeneralSimulationConfig generalConfig;
    private final SimulationConfig config;
    
    public RemoteSimulationImpl(GeneralSimulationConfig generalConfig, SimulationConfig config) {
        this.generalConfig = generalConfig;
        this.config = config;
    }



    @Override
    public RemoteResult call() throws Exception {
        System.out.println(this.generalConfig.getYaml());
        return null;
    }

}
