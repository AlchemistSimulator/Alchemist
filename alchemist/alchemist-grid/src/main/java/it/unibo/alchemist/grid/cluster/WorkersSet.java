package it.unibo.alchemist.grid.cluster;

import java.util.Set;

import it.unibo.alchemist.grid.simulation.RemoteResult;
import it.unibo.alchemist.grid.simulation.SimulationsSet;

public interface WorkersSet {

    /**
     * Distribute and execute the simulation set on set's workers.
     * @param simulationsSet Simulations to execute
     * @return Simulations' results
     */
    Set<RemoteResult> distributeSimulations(SimulationsSet simulationsSet);
}
