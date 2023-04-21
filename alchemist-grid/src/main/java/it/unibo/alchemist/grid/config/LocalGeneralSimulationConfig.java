/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.config;

import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.model.Time;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Local {@link GeneralSimulationConfig} that contains all information in local memory.
 *
 */
public final class LocalGeneralSimulationConfig extends LightInfoGeneralSimulationConfig {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Map<String, byte[]> dependencies;

    /**
     * @param loader Simulation's loader
     * @param endStep Simulation's end step
     * @param endTime Simulation's end time
     */
    public LocalGeneralSimulationConfig(final Loader loader, final long endStep, final Time endTime) {
        super(loader, endStep, endTime);
        this.dependencies = new HashMap<>();
        for (final String file : Objects.requireNonNull(loader).getRemoteDependencies()) {
            try {
                final URL dependency = ResourceLoader.getResource(file);
                if (dependency != null) {
                    dependencies.put(file, Files.readAllBytes(Paths.get(dependency.toURI())));
                } else {
                    throw new IllegalArgumentException("Dependency non exixts: " + file);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Dependency non exixts: " + file, e);
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Failed to get resource URI: " + file, e);
            }
        }
    }

    /**
     * @param loader Simulation's loader
     * @param endTime Simulation's end time
     */
    public LocalGeneralSimulationConfig(final Loader loader, final Time endTime) {
        this(loader, Long.MAX_VALUE, endTime);
    }

    @Override
    public Map<String, byte[]> getDependencies() {
        return Collections.unmodifiableMap(this.dependencies);
    }

}
