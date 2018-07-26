/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
/**
 * 
 */
package it.unibo.alchemist.model.implementations.timedistributions;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * @param <T>
 *            Concentration type
 */
public class Trigger<T> extends AbstractDistribution<T> {

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
    protected void updateStatus(final Time curTime, final boolean executed, final double param, final Environment<T, ?> env) {
        if (dryRunDone && curTime.compareTo(getNextOccurence()) >= 0 && executed) {
            setTau(new DoubleTime(Double.POSITIVE_INFINITY));
        }
        dryRunDone = true;
    }

    @Override
    public Trigger<T> clone(final Time currentTime) {
        return new Trigger<>(getNextOccurence());
    }

}
