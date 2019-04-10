/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.cluster;

import it.unibo.alchemist.grid.simulation.Complexity;

/**
 * The entity that represent the joined cluster.
 *
 */
public interface Cluster extends AutoCloseable {

    /**
     * 
     * @param complexity a simulation's complexity
     * @return Workers' set that can execute a simulation with given complexity 
     */
    WorkerSet getWorkersSet(Complexity complexity);

    /**
     * Leave the cluster.
     */
    void close();
}
