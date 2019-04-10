/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.cluster;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
public final class WorkerSetImpl implements WorkerSet {

    private final ClusterGroup grp;
    private final Ignite ignite;

    /**
     * 
     * @param ignite Ignite instance
     * @param grp workers' group
     */
    public WorkerSetImpl(final Ignite ignite, final ClusterGroup grp) {
        this.grp = Objects.requireNonNull(grp);
        this.ignite = Objects.requireNonNull(ignite);
    }

    @Override
    public Set<RemoteResult> distributeSimulations(final SimulationSet simulationsSet) {
        final IgniteCompute compute = this.ignite.compute(this.grp);
        try (RemoteGeneralSimulationConfig gc = new RemoteGeneralSimulationConfig(simulationsSet.getGeneralSimulationConfig(), this.ignite)) {
            final List<RemoteSimulation<?>> jobs = simulationsSet.getSimulationConfigs().stream()
                    .map(e -> new RemoteSimulationImpl<>(gc, e, ignite.cluster().localNode().id()))
                    .collect(Collectors.toList());
            return new HashSet<>(compute.call(jobs));
        }
    }

}
