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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.UUID;
import java.util.stream.Collectors;

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
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * {@link RemoteSimulation} implementation for Apache Ignite.
 *
 * @param <T>
 * @param <P>
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
                final Environment<T, P> env = loader.getWith(config.getVariables());
                final Simulation<T, P> sim = new Engine<>(env, generalConfig.getEndStep(), generalConfig.getEndTime());
                final Map<String, Object> defaultVars = loader.getVariables().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
                defaultVars.putAll(config.getVariables());
                final String header = config.getVariables().entrySet().stream()
                        .map(e -> e.getKey() + " = " + e.getValue())
                        .collect(Collectors.joining(", "));
                final String filename = masterNodeId.toString() + "_" + config.toString() + ".txt";
                final Exporter<T, P> exp = new Exporter<>(wd.getFileAbsolutePath(filename),
                        1, header, loader.getDataExtractors());
                sim.addOutputMonitor(exp);
                sim.play();
                sim.run();
                return new RemoteResultImpl(wd.getFileContent(filename),
                        Ignition.ignite().cluster().localNode().id(), sim.getError(), config);
            };
            final FutureTask<RemoteResultImpl> futureTask = new FutureTask<>(callable);
            final Thread t = new Thread(futureTask);
            final URLClassLoader cl = new URLClassLoader(new URL[]{wd.getDirectoryUrl()},
                    ResourceLoader.getClassLoader());
            t.setContextClassLoader(cl);
            t.start();
            return futureTask.get();
        } catch (SecurityException | IllegalArgumentException
                | IOException | InterruptedException | ExecutionException e1) {
            throw new IllegalStateException(e1);
        }
    }
}
