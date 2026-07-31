/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions;

import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.observation.MutableObservable;
import it.unibo.alchemist.model.observation.Observable;

import javax.annotation.Nonnull;
import java.io.Serial;

/**
 * This class provides, through a template method pattern, an utility that
 * ensures that the distribution does not trigger events before its initial
 * scheduling time.
 *
 * @param <T> concentration type
 */
public abstract class AbstractDistribution<T> implements TimeDistribution<T> {

    @Serial
    private static final long serialVersionUID = -8906648194668569179L;
    private final MutableObservable<Time> tau;
    private boolean schedulable;
    private final Time startTime;

    /**
     * @param start
     *            initial time
     */
    public AbstractDistribution(final Time start) {
        tau = MutableObservable.Companion.observe(start, false);
        startTime = start;
    }

    /**
     * Allows subclasses to set the next putative time. Use with care.
     *
     * @param t
     *            the new time
     */
    protected final void setNextOccurrence(final Time t) {
        tau.setCurrent(t);
    }

    @Override
    public final void update(
            final @Nonnull Time currentTime,
            final @Nonnull Actionable<T> source
    ) {
        update(currentTime, true, source);
    }

    @Override
    public final void reactToUpdate(final @Nonnull Time currentTime, final @Nonnull Actionable<T> source) {
        update(currentTime, false, source);
    }

    private void update(final Time currentTime, final boolean executed, final Actionable<T> source) {
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
        updateStatus(schedulable ? currentTime : startTime, executed, source);
    }

    @Override
    public final Observable<Time> getNextOccurence() {
        return tau;
    }

    /**
     * Implement this method to update the distribution's internal status.
     *
     * @param currentTime
     *            current time
     * @param executed
     *            true if the reaction whose this distribution has been
     *            associated has just been executed
     * @param source the actionable associated with this distribution
     */
    protected abstract void updateStatus(Time currentTime, boolean executed, Actionable<T> source);

    @Override
    public abstract AbstractDistribution<T> cloneOnNewNode(@Nonnull Node<T> destination, @Nonnull Time currentTime);

}
