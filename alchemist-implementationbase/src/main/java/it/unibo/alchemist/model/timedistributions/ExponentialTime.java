/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.timedistributions;

import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.times.DoubleTime;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Time;

/**
 * Markovian events.
 * 
 * @param <T> concentration type
 */
public class ExponentialTime<T> extends AbstractDistribution<T> {

    private static final long serialVersionUID = 5216987069271114818L;
    private double oldPropensity = -1;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator randomGenerator;
    private final double rate;

    /**
     * @param markovianRate
     *            Markovian rate for this distribution
     * @param randomGenerator
     *            {@link RandomGenerator} used internally
     */
    public ExponentialTime(final double markovianRate, final RandomGenerator randomGenerator) {
        this(markovianRate, Time.ZERO, randomGenerator);
    }

    /**
     * @param markovianRate
     *            Markovian rate for this distribution
     * @param start
     *            initial time
     * @param randomGenerator
     *            {@link RandomGenerator} used internally
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public ExponentialTime(final double markovianRate, final Time start, final RandomGenerator randomGenerator) {
        super(start);
        rate = markovianRate;
        this.randomGenerator = randomGenerator;
    }

    @Override
    public final void updateStatus(
            final Time currentTime,
            final boolean executed,
            final double newpropensity,
            final Environment<T, ?> environment) {
        assert !Double.isNaN(newpropensity);
        assert !Double.isNaN(oldPropensity);
        if (oldPropensity == 0 && newpropensity != 0) {
            update(newpropensity, true, currentTime);
        } else if (oldPropensity != 0 && newpropensity != 0) {
            update(newpropensity, executed, currentTime);
        } else if (oldPropensity != 0 && newpropensity == 0) {
            setNextOccurrence(Time.INFINITY);
        }
        oldPropensity = newpropensity;
    }

    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    private void update(final double newpropensity, final boolean isMu, final Time curTime) {
        assert !Double.isNaN(newpropensity);
        assert !Double.isNaN(oldPropensity);
        if (isMu) {
            final Time dt = genTime(newpropensity);
            setNextOccurrence(curTime.plus(dt));
        } else {
            if (oldPropensity != newpropensity) {
                final Time sub = getNextOccurence().minus(curTime);
                final Time mul = sub.times(oldPropensity / newpropensity);
                setNextOccurrence(mul.plus(curTime));
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
        return -FastMath.log1p(-randomGenerator.nextDouble()) / lambda;
    }

    /**
     * Must be overridden by subclasses returning the correct instance.
     *
     * @param currentTime the time at which the time distribution was cloned
     * @return a new ExponentialTime
     */
    @Override
    public ExponentialTime<T> cloneOnNewNode(final Node<T> destination, final Time currentTime) {
        return new ExponentialTime<>(rate, Time.ZERO, randomGenerator);
    }

    /**
     *
     * @return the rate of the reaction
     */
    @Override
    public double getRate() {
        return rate;
    }

}
