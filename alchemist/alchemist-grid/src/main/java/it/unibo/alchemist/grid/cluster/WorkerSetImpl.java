package it.unibo.alchemist.grid.cluster;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.cluster.ClusterGroup;

import it.unibo.alchemist.grid.config.RemoteGeneralSimulationConfig;
import it.unibo.alchemist.grid.simulation.RemoteResult;
import it.unibo.alchemist.grid.simulation.RemoteSimulation;
import it.unibo.alchemist.grid.simulation.RemoteSimulationImpl;
import it.unibo.alchemist.grid.simulation.SimulationSet;

/**
 * Implementation of {@link WorkerSet} which uses Apache Ignite.
 *
 */
public class WorkerSetImpl implements WorkerSet {

    private final ClusterGroup grp;
    private final Ignite ignite;

    /**
     * 
     * @param ignite Ignite instance
     * @param grp workers' group
     */
    public WorkerSetImpl(final Ignite ignite, final ClusterGroup grp) {
        this.grp = grp;
        this.ignite = ignite;
    }

    @Override
    public Set<RemoteResult> distributeSimulations(final SimulationSet<?> simulationsSet) {
        final IgniteCompute compute = this.ignite.compute(this.grp);
        try (RemoteGeneralSimulationConfig<?> gc = new RemoteGeneralSimulationConfig<>(simulationsSet.getGeneralSimulationConfig(), this.ignite)) {
            final List<RemoteSimulation<?>> jobs = simulationsSet.getSimulationConfigs().stream()
                    .map(e -> new RemoteSimulationImpl<>(gc, e, ignite.cluster().localNode().id()))
                    .collect(Collectors.toList());
            return new HashSet<>(compute.call(jobs));
        }
    }

}
