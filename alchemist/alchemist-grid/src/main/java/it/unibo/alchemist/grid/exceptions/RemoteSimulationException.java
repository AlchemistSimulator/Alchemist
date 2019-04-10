/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.exceptions;

import java.util.UUID;
import java.util.stream.Collectors;

import it.unibo.alchemist.grid.config.SimulationConfig;

/**
 * 
 *
 */
public class RemoteSimulationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public RemoteSimulationException() {
        super();
    }

    /**
     * 
     * @param nodeId UUID of node that has run the simulation
     * @param simulationConfig Simulation's config
     * @param throwable Error that made the simulation fail
     */
    public RemoteSimulationException(final UUID nodeId, final SimulationConfig simulationConfig, 
            final Throwable throwable) {
        super("Error for simulation with variables: " + simulationConfig.getVariables().entrySet().stream()
                .map(e -> e.getKey() + '=' + e.getValue())
                .collect(Collectors.joining(" ")) + "in node: " + nodeId, throwable);
    }
}
