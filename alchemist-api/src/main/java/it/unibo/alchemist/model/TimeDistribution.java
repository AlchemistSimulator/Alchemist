/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model;

import it.unibo.alchemist.model.observation.Observable;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * This interface represents a temporal distribution for any event.
 *
 * @param <T>
 *            concentration type
 */
public interface TimeDistribution<T> extends Cloneable, Serializable {

    /**
     * Updates the internal status.
     *
     * @param currentTime
     *            current time
     * @param source
     *            the host reaction
     */
    void update(
        @Nonnull Time currentTime,
        @Nonnull Actionable<T> source
    );

    /**
     * Reacts to a model change that may alter the source propensity without advancing the event stream.
     * Implementations which are insensitive to model changes may keep the default no-op behavior.
     *
     * @param currentTime current simulation time
     * @param source the host reaction
     */
    default void reactToUpdate(@Nonnull final Time currentTime, @Nonnull final Actionable<T> source) { }

    /**
     * @return the next time at which the event will occur
     */
    Observable<Time> getNextOccurence();

    /**
     * @return how many times per time unit the event will happen on average
     */
    double getRate();

    /**
     * @param destination the node where the newly created time distribution will be placed
     * @param currentTime
     *            the time at which the cloning operation happened
     * @return an exact copy of this {@link TimeDistribution}
     */
    TimeDistribution<T> cloneOnNewNode(
        @Nonnull Node<T> destination,
        @Nonnull Time currentTime
    );

}
