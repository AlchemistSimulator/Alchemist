package it.unibo.alchemist.grid.cluster;

import it.unibo.alchemist.grid.simulation.Complexity;

public interface Cluster {
    public WorkersSet getWorkersSet(Complexity complexity);
}
