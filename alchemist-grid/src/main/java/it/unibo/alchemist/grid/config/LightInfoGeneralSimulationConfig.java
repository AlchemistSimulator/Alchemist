/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.grid.config;

import it.unibo.alchemist.boundary.Loader;
import it.unibo.alchemist.model.Time;

import java.util.Map;
import java.util.Objects;

/**
 *  Abstract simulation config that contains small serializable informations.
 * 
 */
public abstract class LightInfoGeneralSimulationConfig implements GeneralSimulationConfig {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final long endStep;
    private final Time endTime;
    private final Loader loader;

    /**
     * @param endStep Simulation's end step
     * @param endTime Simulation's end time
     * @param loader Simulation's loader
     */
    public LightInfoGeneralSimulationConfig(final Loader loader, final long endStep, final Time endTime) {
        this.endStep = endStep;
        this.endTime = Objects.requireNonNull(endTime);
        this.loader = Objects.requireNonNull(loader);
    }

    /**
     * @param endTime Simulation's end time
     * @param loader Simulation's loader
     */
    public LightInfoGeneralSimulationConfig(final Loader loader, final Time endTime) {
        this(loader, Long.MAX_VALUE, endTime);
    }

    /**
     * @param loader Simulation's loader
     */
    public LightInfoGeneralSimulationConfig(final Loader loader) {
        this(loader, Time.INFINITY);
    }

    @Override
    public final Loader getLoader() {
        return this.loader;
    }

    @Override
    public abstract Map<String, byte[]> getDependencies();

    @Override
    public final long getEndStep() {
        return endStep;
    }

    @Override
    public final Time getEndTime() {
        return endTime;
    }
}
