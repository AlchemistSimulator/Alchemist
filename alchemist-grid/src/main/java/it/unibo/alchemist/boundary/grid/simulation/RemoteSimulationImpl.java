/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.simulation;

import it.unibo.alchemist.boundary.Loader;
import it.unibo.alchemist.boundary.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.boundary.grid.config.SimulationConfig;
import it.unibo.alchemist.boundary.grid.util.WorkingDirectory;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.terminators.AfterTime;
import it.unibo.alchemist.model.terminators.StepCount;
import org.apache.ignite.Ignition;
import org.kaikikm.threadresloader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serial;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * {@link RemoteSimulation} implementation for Apache Ignite.
 *
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public final class RemoteSimulationImpl<T, P extends Position<P>> implements RemoteSimulation<T> {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger L = LoggerFactory.getLogger(RemoteSimulationImpl.class);
    private final GeneralSimulationConfig generalConfig;
    private final SimulationConfig config;
    private final UUID masterNodeId;
    /**
     * 
     * @param generalConfig General simulation config
     * @param config Simulation's specific configs
     * @param masterNodeId The node that started the computation
     */
    public RemoteSimulationImpl(final GeneralSimulationConfig generalConfig, final SimulationConfig config,
            final UUID masterNodeId) {
        this.generalConfig = Objects.requireNonNull(generalConfig);
        this.config = Objects.requireNonNull(config);
        this.masterNodeId = Objects.requireNonNull(masterNodeId);
    }



    @Override
    public RemoteResult call() {
        L.debug("Executing simulation for variables: " + config.getVariables());
        try (WorkingDirectory wd = new WorkingDirectory()) {
            wd.writeFiles(this.generalConfig.getDependencies());
            final Callable<RemoteResultImpl> callable = () -> {
                ResourceLoader.injectURLs(wd.getDirectoryUrl());
                final Loader loader = generalConfig.getLoader();
                final Simulation<T, P> simulation = loader.getWith(config.getVariables());
                final Environment<T, P> environment = simulation.getEnvironment();
                environment.addTerminator(new StepCount<>(generalConfig.getEndStep()));
                environment.addTerminator(new AfterTime<>(generalConfig.getEndTime()));
                final String filename = masterNodeId + "_" + config + ".txt";
                simulation.play();
                simulation.run();
                try (var ignite = Ignition.ignite()) {
                    return new RemoteResultImpl(
                        wd.getFileContent(filename),
                        ignite.cluster().localNode().id(),
                        simulation.getError(),
                        config
                    );
                }
            };
            final FutureTask<RemoteResultImpl> futureTask = new FutureTask<>(callable);
            final Thread t = new Thread(futureTask);
            final URLClassLoader cl = new URLClassLoader(
                new URL[]{wd.getDirectoryUrl()},
                ResourceLoader.getClassLoader()
            );
            t.setContextClassLoader(cl);
            t.start();
            return futureTask.get();
        } catch (SecurityException | IllegalArgumentException
                | IOException | InterruptedException | ExecutionException e1) {
            throw new IllegalStateException(e1);
        }
    }
}
