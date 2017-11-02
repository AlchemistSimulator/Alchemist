/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

import java.io.Serializable;

/**
 * An interface for the visualization of the simulation.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 */
public interface OutputMonitor<T> extends Serializable {

    /**
     * This method will be called by the simulation once the whole simulation
     * has finished, either because it reached its latest point or because the
     * user stopped it. Thread safety note: no specific policy is defined for
     * the control flow which will execute this method. A new thread could have
     * been spawned or the same flow of the simulation may execute this method.
     * This depends on the specific {@link Simulation} implementation.
     *
     * @param environment  The current environment
     * @param time The time at which the simulation ended
     * @param step The last step number
     */
    void finished(Environment<T> environment, Time time, long step);

    /**
     * This method will be called by the simulation as soon as the
     * initialization phase is completed. Thread safety note: no specific policy
     * is defined for the control flow which will execute this method. A new
     * thread could have been spawned or the same flow of the simulation may
     * execute this method. This depends on the specific {@link Simulation}
     * implementation.
     *
     * @param environment the environment
     */
    void initialized(Environment<T> environment);

    /**
     * This method will be called by the simulation every time a simulation step
     * is done. Thread safety note: no specific policy is defined for the
     * control flow which will execute this method. A new thread could have been
     * spawned or the same flow of the simulation may execute this method. This
     * depends on the specific {@link Simulation} implementation.
     *
     * @param environment  The current environment
     * @param reaction    The last reaction executed
     * @param time The time at this simulation point
     * @param step The current simulation step
     */
    void stepDone(Environment<T> environment, Reaction<T> reaction, Time time, long step);

}
