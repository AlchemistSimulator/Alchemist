/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.cluster;

import it.unibo.alchemist.boundary.grid.simulation.Complexity;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;

import java.nio.file.Path;
import java.util.Objects;

/**
 * An implementation of {@link Cluster}  uses Apache Ignite. 
 *
 */
public final class ClusterImpl implements Cluster {

    private static final int IGNITE_RAM_MULT_FACTOR = 1_000_000_000;
    private final Ignite ignite;

    /**
     * 
     * @param configPath path of Ignite's configuration file
     */
    public ClusterImpl(final Path configPath) {
        Ignition.setClientMode(true);
        this.ignite = Ignition.start(Objects.requireNonNull(configPath).toString());
    }

    @Override
    public WorkerSet getWorkersSet(final Complexity complexity) {
        final ClusterGroup grp = ignite.cluster()
                .forServers()
                .forPredicate((node) 
                        -> node.metrics().getHeapMemoryTotal() >= complexity.getRamUsage() * IGNITE_RAM_MULT_FACTOR);
        return new WorkerSetImpl(ignite, grp);
    }

    @Override
    public void close() {
        ignite.close();
    }

}
