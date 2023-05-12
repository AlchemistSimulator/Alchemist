/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.cluster;

import java.util.Set;

import it.unibo.alchemist.boundary.grid.simulation.RemoteResult;
import it.unibo.alchemist.boundary.grid.simulation.SimulationSet;

/**
 * Set of remote nodes that can run simulations.
 *
 */
public interface WorkerSet {

    /**
     * Distribute and execute the simulation set on set's workers.
     * @param simulationsSet Simulations to execute
     * @return Simulations' results
     */
    Set<RemoteResult> distributeSimulations(SimulationSet simulationsSet);
}
