/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.timedistributions;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * This class provides, through a template method pattern, an utility that
 * ensures that the distribution does not trigger events before its initial
 * scheduling time.
 * 
 * @param <T>
 */
public abstract class AbstractDistribution<T> implements TimeDistribution<T> {

    private static final long serialVersionUID = -8906648194668569179L;
    private Time tau;
    private boolean schedulable;
    private final Time startTime;

    /**
     * @param start
     *            initial time
     */
    public AbstractDistribution(final Time start) {
        tau = start;
        startTime = start;
    }

    /**
     * Allows subclasses to set the next putative time. Use with care.
     * 
     * @param t
     *            the new time
     */
    protected final void setTau(final Time t) {
        this.tau = t;
    }

    @Override
    public final void update(final Time currentTime, final boolean executed, final double param, final Environment<T, ?> environment) {
        if (!schedulable && currentTime.compareTo(startTime) >= 0) {
            /*
             * If the simulation time is beyond the startTime for this reaction,
             * it can start being scheduled normally.
             */
            schedulable = true;
        }
        /*
         * If the current time is not past the starting time for this reaction,
         * it should not be used.
         */
        updateStatus(schedulable ? currentTime : startTime, executed, param, environment);
    }

    @Override
    public final Time getNextOccurence() {
        return tau;
    }

    /**
     * Implement this method to update the distribution's internal status.
     * 
     * @param curTime
     *            current time
     * @param executed
     *            true if the reaction whose this distribution has been
     *            associated has just been executed
     * @param param
     *            optional parameter passed by the reaction
     * @param env
     *            the current environment
     */
    protected abstract void updateStatus(Time curTime, boolean executed, double param, Environment<T, ?> env);

    @Override
    public abstract AbstractDistribution<T> clone(Time currentTime);

}
