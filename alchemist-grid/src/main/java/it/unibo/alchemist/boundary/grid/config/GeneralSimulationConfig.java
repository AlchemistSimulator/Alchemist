/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.grid.config;

import java.io.Serializable;
import java.util.Map;

import it.unibo.alchemist.boundary.Loader;
import it.unibo.alchemist.model.Time;
/**
 * Simulation's configs valid for more than one simulation.
 */
public interface GeneralSimulationConfig extends Serializable {
    /**
     * 
     * @return simulation's yaml as string
     */
    Loader getLoader();
    /**
     * 
     * @return Map with dependencies files path as key and their content as value
     */
    Map<String, byte[]> getDependencies();
    /**
     * 
     * @return Simulation's end step
     */
    long getEndStep();
    /**
     * 
     * @return Simulation's end time
     */
    Time getEndTime();
}
