/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

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
     * @param executed
     *            true if the reaction has just been executed
     * @param param
     *            a parameter passed by the reaction
     * @param environment
     *            the current environment
     */
    void update(Time currentTime, boolean executed, double param, Environment<T, ?> environment);

    /**
     * @return the next time at which the event will occur
     */
    Time getNextOccurence();

    /**
     * @return how many times per time unit the event will happen on average
     */
    double getRate();

    /**
     * @param currentTime
     *            the time at which the cloning operation happened
     * @return an exact copy of this {@link TimeDistribution}
     */
    TimeDistribution<T> clone(Time currentTime);

}
