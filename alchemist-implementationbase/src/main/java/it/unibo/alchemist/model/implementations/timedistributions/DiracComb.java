/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.timedistributions;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Time;

/**
 * A DiracComb is a sequence of events that happen every fixed time interval.
 * 
 * @param <T> concentration type
 */
public class DiracComb<T> extends AbstractDistribution<T> {

    private static final long serialVersionUID = -5382454244629122722L;

    private final double timeInterval;

    /**
     * @param start
     *            initial time
     * @param rate
     *            how many events should happen per time unit
     */
    public DiracComb(final Time start, final double rate) {
        super(start);
        timeInterval = 1 / rate;
    }

    /**
     * @param rate
     *            how many events should happen per time unit
     */
    public DiracComb(final double rate) {
        this(new DoubleTime(), rate);
    }

    @Override
    public final double getRate() {
        return 1 / timeInterval;
    }

    @Override
    protected final void updateStatus(
            final Time currentTime,
            final boolean executed,
            final double param,
            final Environment<T, ?> environment) {
        if (executed) {
            setNextOccurrence(new DoubleTime(currentTime.toDouble() + timeInterval));
        }
    }

    @Override
    public final DiracComb<T> cloneOnNewNode(final Node<T> destination, final Time currentTime) {
        return new DiracComb<>(currentTime, 1 / timeInterval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ΔT=" + timeInterval + "]";
    }

}
