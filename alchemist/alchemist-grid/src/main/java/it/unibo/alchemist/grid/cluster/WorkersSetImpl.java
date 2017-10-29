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
import it.unibo.alchemist.grid.simulation.SimulationsSet;

/**
 * Implementation of {@link WorkersSet} which uses Apache Ignite.
 *
 */
public class WorkersSetImpl implements WorkersSet {

    private final ClusterGroup grp;
    //TODO mi passo direttamente il compute?
    private final Ignite ignite;

    /**
     * 
     * @param ignite Ignite instance
     * @param grp workers' group
     */
    public WorkersSetImpl(final Ignite ignite, final ClusterGroup grp) {
        this.grp = grp;
        this.ignite = ignite;
    }

    @Override
    public Set<RemoteResult> distributeSimulations(final SimulationsSet<?> simulationsSet) {
        final IgniteCompute compute = this.ignite.compute(this.grp);
        try (RemoteGeneralSimulationConfig<?> gc = new RemoteGeneralSimulationConfig<>(simulationsSet.getGeneralSimulationConfig(), this.ignite)) {
            final List<RemoteSimulation<?>> jobs = simulationsSet.getSimulationConfigs().stream()
                    .map(e -> new RemoteSimulationImpl<>(gc, e))
                    .collect(Collectors.toList());
            //TODO ricorda hash nella classe
            return new HashSet<>(compute.call(jobs));
        }
    }

}
