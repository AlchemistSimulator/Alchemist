/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.timedistributions;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Markovian events.
 * 
 * @param <T>
 */
public class ExponentialTime<T> extends AbstractDistribution<T> {

    private static final long serialVersionUID = 5216987069271114818L;
    private double oldPropensity = -1;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator rand;
    private final double rate;

    /**
     * @param markovianRate
     *            Markovian rate for this distribution
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public ExponentialTime(final double markovianRate, final RandomGenerator random) {
        this(markovianRate, DoubleTime.ZERO_TIME, random);
    }

    /**
     * @param markovianRate
     *            Markovian rate for this distribution
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public ExponentialTime(final double markovianRate, final Time start, final RandomGenerator random) {
        super(start);
        rate = markovianRate;
        rand = random;
    }

    @Override
    public void updateStatus(
            final Time curTime,
            final boolean executed,
            final double newpropensity,
            final Environment<T, ?> env) {
        assert !Double.isNaN(newpropensity);
        assert !Double.isNaN(oldPropensity);
        if (oldPropensity == 0 && newpropensity != 0) {
            update(newpropensity, true, curTime);
        } else if (oldPropensity != 0 && newpropensity != 0) {
            update(newpropensity, executed, curTime);
        } else if (oldPropensity != 0 && newpropensity == 0) {
            setTau(DoubleTime.INFINITE_TIME);
        }
        oldPropensity = newpropensity;
    }

    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    private void update(final double newpropensity, final boolean isMu, final Time curTime) {
        assert !Double.isNaN(newpropensity);
        assert !Double.isNaN(oldPropensity);
        if (isMu) {
            final Time dt = genTime(newpropensity);
            setTau(curTime.plus(dt));
        } else {
            if (oldPropensity != newpropensity) {
                final Time sub = getNextOccurence().minus(curTime);
                final Time mul = sub.times(oldPropensity / newpropensity);
                setTau(mul.plus(curTime));
            }
        }
    }

    /**
     * @param propensity
     *            the current propensity for the reaction
     * @return the next occurrence time for the reaction, in case this is the
     *         reaction which have been executed.
     */
    protected Time genTime(final double propensity) {
        return new DoubleTime(uniformToExponential(propensity));
    }

    private double uniformToExponential(final double lambda) {
        return -FastMath.log1p(-rand.nextDouble()) / lambda;
    }

    @Override
    public ExponentialTime<T> clone(final Time currentTime) {
        return new ExponentialTime<>(rate, DoubleTime.ZERO_TIME, rand);
    }

    @Override
    public double getRate() {
        return rate;
    }

}
