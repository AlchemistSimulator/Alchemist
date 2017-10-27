package it.unibo.alchemist.grid.cluster;

import java.nio.file.Path;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import it.unibo.alchemist.grid.simulation.Complexity;

public class ClusterImpl implements Cluster {

    private final Ignite ignite;
    
    public ClusterImpl(Path configPath) {
        //TODO e l'eccezione????
        this.ignite = Ignition.start(configPath.toString()); 
    }
    
    @Override
    public WorkersSet getWorkersSet(Complexity complexity) {
        return new WorkersSetImpl(ignite, ignite.cluster().forPredicate((node) -> node.metrics().getHeapMemoryTotal() >= complexity.getRamUsage()));
    }

}
