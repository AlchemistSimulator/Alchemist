package it.unibo.alchemist.grid.cluster;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.cluster.ClusterGroup;
import it.unibo.alchemist.grid.simulation.RemoteResult;
import it.unibo.alchemist.grid.simulation.RemoteSimulation;
import it.unibo.alchemist.grid.simulation.RemoteSimulationImpl;
import it.unibo.alchemist.grid.simulation.SimulationsSet;

public class WorkersSetImpl implements WorkersSet {
    
    private final ClusterGroup grp;
    //TODO mi passo direttamente il compute?
    private final Ignite ignite;

    public WorkersSetImpl(Ignite ignite, ClusterGroup grp) {
        this.grp = grp;
        this.ignite = ignite;
    }
    
    @Override
    public Set<RemoteResult> distributeSimulations(SimulationsSet simulationsSet) {
        IgniteCompute compute = this.ignite.compute(this.grp);
        List<RemoteSimulation> jobs = simulationsSet.getSimulationConfigs().stream().map(e -> new RemoteSimulationImpl(simulationsSet.getGeneralSimulationConfig(), e)).collect(Collectors.toList());
        //TODO ricorda hash nella classe
        return new HashSet<>(compute.call(jobs));
    }

}
