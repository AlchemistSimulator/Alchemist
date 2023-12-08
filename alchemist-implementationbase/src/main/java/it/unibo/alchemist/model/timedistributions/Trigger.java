/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.timedistributions;

import it.unibo.alchemist.model.times.DoubleTime;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Time;

/**
 * @param <T>
 *            Concentration type
 */
public final class Trigger<T> extends AbstractDistribution<T> {

    private static final long serialVersionUID = 5207992119302525618L;
    private boolean dryRunDone;

    /**
     * @param event
     *            the time at which the event will happen
     */
    public Trigger(final Time event) {
        super(event);
    }

    @Override
    public double getRate() {
        return Double.NaN;
    }

    @Override
    protected void updateStatus(
        final Time currentTime,
        final boolean executed,
        final double param,
        final Environment<T, ?> environment
    ) {
        if (dryRunDone && currentTime.compareTo(getNextOccurence()) >= 0 && executed) {
            setNextOccurrence(new DoubleTime(Double.POSITIVE_INFINITY));
        }
        dryRunDone = true;
    }

    @Override
    public Trigger<T> cloneOnNewNode(final Node<T> destination, final Time currentTime) {
        return new Trigger<>(getNextOccurence());
    }

}
