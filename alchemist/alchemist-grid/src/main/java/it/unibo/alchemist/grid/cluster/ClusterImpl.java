package it.unibo.alchemist.grid.cluster;

import java.nio.file.Path;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.spi.deployment.local.LocalDeploymentSpi;

import it.unibo.alchemist.grid.simulation.Complexity;

/**
 * An implementation of {@link Cluster}  uses Apache Ignite. 
 *
 */
public class ClusterImpl implements Cluster {

    private static final int IGNITE_RAM_MULT_FACTOR = 1000000000;
    private final Ignite ignite;

    /**
     * 
     * @param configPath path of Ignite's configuration file
     */
    public ClusterImpl(final Path configPath) {
        Ignition.setClientMode(true);
        this.ignite = Ignition.start(configPath.toString());
    }

    @Override
    public WorkersSet getWorkersSet(final Complexity complexity) {
        return new WorkersSetImpl(ignite, ignite.cluster().forPredicate((node) -> node.metrics().getHeapMemoryTotal() >= complexity.getRamUsage() * IGNITE_RAM_MULT_FACTOR));
    }

    @Override
    public void close() {
        ignite.close();
    }

}
