/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Time;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

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
     * specific {@link it.unibo.alchemist.core.Simulation} implementation.
     * 
     * @param environment
     *            The current environment
     * @param time
     *            The time at which the simulation ended
     * @param step
     *            The last step number
     */
    default void finished(
        @Nonnull final Environment<T, P> environment,
        @Nonnull final Time time,
        final long step
    ) { }

    /**
     * This method will be called by the simulation as soon as the initialization
     * phase is completed. Thread safety note: no specific policy is defined for the
     * control flow which will execute this method. A new thread could have been
     * spawned or the same flow of the simulation may execute this method. This
     * depends on the specific {@link it.unibo.alchemist.core.Simulation} implementation.
     *
     * @param environment
     *            the environment
     */
    default void initialized(@Nonnull final Environment<T, P> environment) { }

    /**
     * This method will be called by the simulation every time a simulation step is
     * done. Thread safety note: no specific policy is defined for the control flow
     * which will execute this method. A new thread could have been spawned or the
     * same flow of the simulation may execute this method. This depends on the
     * specific {@link it.unibo.alchemist.core.Simulation} implementation.
     * 
     * @param environment
     *            The current environment
     * @param reaction
     *            The last reaction executed
     * @param time
     *            The time at this simulation point
     * @param step
     *            The current simulation step
     */
    default void stepDone(
        @Nonnull final Environment<T, P> environment,
        @Nullable final Actionable<T> reaction,
        @Nonnull final Time time,
        final long step
    ) { }

}
