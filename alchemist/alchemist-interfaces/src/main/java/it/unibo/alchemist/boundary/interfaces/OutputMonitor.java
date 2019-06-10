/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.interfaces;

import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 * An interface for the visualization of the simulation.
 *
 * @param <T> Concentration Type
 * @param <P> {@link Position} Type
 */
public interface OutputMonitor<T, P extends Position<? extends P>> extends Serializable {

    /**
     * This method will be called by the simulation once the whole simulation has
     * finished, either because it reached its latest point or because the user
     * stopped it. Thread safety note: no specific policy is defined for the control
     * flow which will execute this method. A new thread could have been spawned or
     * the same flow of the simulation may execute this method. This depends on the
     * specific {@link it.unibo.alchemist.core.interfaces.Simulation} implementation.
     * 
     * @param env
     *            The current environment
     * @param time
     *            The time at which the simulation ended
     * @param step
     *            The last step number
     */
    void finished(Environment<T, P> env, Time time, long step);

    /**
     * This method will be called by the simulation as soon as the initialization
     * phase is completed. Thread safety note: no specific policy is defined for the
     * control flow which will execute this method. A new thread could have been
     * spawned or the same flow of the simulation may execute this method. This
     * depends on the specific {@link it.unibo.alchemist.core.interfaces.Simulation} implementation.
     *
     * @param env
     *            the environment
     */
    void initialized(Environment<T, P> env);

    /**
     * This method will be called by the simulation every time a simulation step is
     * done. Thread safety note: no specific policy is defined for the control flow
     * which will execute this method. A new thread could have been spawned or the
     * same flow of the simulation may execute this method. This depends on the
     * specific {@link it.unibo.alchemist.core.interfaces.Simulation} implementation.
     * 
     * @param env
     *            The current environment
     * @param r
     *            The last reaction executed
     * @param time
     *            The time at this simulation point
     * @param step
     *            The current simulation step
     */
    void stepDone(Environment<T, P> env, Reaction<T> r, Time time, long step);

}
