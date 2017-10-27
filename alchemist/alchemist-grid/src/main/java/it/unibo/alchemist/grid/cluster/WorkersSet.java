package it.unibo.alchemist.grid.cluster;

import java.util.Set;

import it.unibo.alchemist.grid.simulation.RemoteResult;
import it.unibo.alchemist.grid.simulation.SimulationsSet;

public interface WorkersSet {
    public Set<RemoteResult> distributeSimulations(SimulationsSet simulationsSet);
}
