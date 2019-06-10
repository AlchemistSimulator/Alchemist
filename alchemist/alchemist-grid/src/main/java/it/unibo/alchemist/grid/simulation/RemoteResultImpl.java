/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.simulation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.google.common.base.Charsets;

import it.unibo.alchemist.grid.config.SimulationConfig;
import it.unibo.alchemist.grid.exceptions.RemoteSimulationException;

/**
 * {@link RemoteResult} implementation.
 *
 */
public final class RemoteResultImpl implements RemoteResult {

    private final String result;
    private final UUID workerNode;
    private final Optional<Throwable> simulationErrors;
    private final SimulationConfig config;

    /**
     * 
     * @param result Result file's content as string
     * @param workerNode UUID of worker node that has done the simulation
     * @param simulationErrors Simulation's errors
     * @param config Simulation's specific config
     */
    public RemoteResultImpl(final String result, final UUID workerNode, final Optional<Throwable> simulationErrors,
            final SimulationConfig config) {
        this.result = Objects.requireNonNull(result);
        this.workerNode = Objects.requireNonNull(workerNode);
        this.simulationErrors = Objects.requireNonNull(simulationErrors);
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public void saveLocally(final String targetFile) throws RemoteSimulationException, FileNotFoundException {
        if (simulationErrors.isPresent()) {
            throw new RemoteSimulationException(this.workerNode, this.config, simulationErrors.get());
        }
        final String target = targetFile + "_" + this.config.toString() + ".txt";
        try (PrintStream out = new PrintStream(target, Charsets.UTF_8.name())) {
            out.print(result);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("There is a bug in Alchemist, in " + getClass(), e);
        }
    }

    @Override
    public int hashCode() {
        return this.config.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RemoteResultImpl) {
            final RemoteResultImpl other = (RemoteResultImpl) obj;
            return config.equals(other.config) && result.equals(other.result);
        }
        return false;
    }

}
