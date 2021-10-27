/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.simulation;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.UUID;

import it.unibo.alchemist.loader.InitializedEnvironment;
import it.unibo.alchemist.loader.export.GlobalExporter;
import org.apache.ignite.Ignition;
import org.kaikikm.threadresloader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;
import it.unibo.alchemist.grid.util.WorkingDirectory;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

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
                final InitializedEnvironment<T, P> initialized = loader.getWith(config.getVariables());
                final Environment<T, P> environment = initialized.getEnvironment();
                final Simulation<T, P> simulation = new Engine<>(
                        environment,
                        generalConfig.getEndStep(),
                        generalConfig.getEndTime()
                );
                final String filename = masterNodeId.toString() + "_" + config.toString() + ".txt";
                simulation.addOutputMonitor(new GlobalExporter<>(initialized.getExporters()));
                simulation.play();
                simulation.run();
                return new RemoteResultImpl(wd.getFileContent(filename),
                        Ignition.ignite().cluster().localNode().id(), simulation.getError(), config);
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
