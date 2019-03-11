/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.export;

import java.util.List;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * An object that is able to extract numeric informations from an Alchemist
 * {@link Environment}, given the current
 * {@link it.unibo.alchemist.core.interfaces.Simulation} {@link Time}, the last
 * {@link Reaction} executed and the current simulation step.
 */
public interface Extractor {

    /**
     * Extracts numeric properties from an environment.
     * 
     * @param env
     *            the {@link Environment}
     * @param r
     *            the last executed {@link Reaction}
     * @param time
     *            the current {@link Time}
     * @param step
     *            the simulation step
     * @return the extracted properties
     */
    double[] extractData(Environment<?, ?> env, Reaction<?> r, Time time, long step);

    /**
     * @return the name of the properties that this {@link Extractor} can
     *         provide
     */
    List<String> getNames();

}
