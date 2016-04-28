/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.timedistributions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * A DiracComb is a sequence of events that happen every fixed time interval.
 * 
 * @param <T>
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
    public double getRate() {
        return 1 / timeInterval;
    }

    @Override
    protected void updateStatus(
            final Time curTime,
            final boolean executed,
            final double param,
            final Environment<T> env) {
        if (executed) {
            setTau(new DoubleTime(curTime.toDouble() + timeInterval));
        }
    }

    @Override
    @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
    public DiracComb<T> clone() {
        return new DiracComb<>(getNextOccurence(), 1 / timeInterval);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " every " + timeInterval;
    }

}
