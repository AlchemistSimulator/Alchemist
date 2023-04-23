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
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.util.RealDistributionUtil;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Time;

/**
 * This class is able to use any distribution provided by Apache Math 3 as a
 * subclass of {@link RealDistribution}, blocking the execution if
 * {@link it.unibo.alchemist.model.Condition#getPropensityContribution()}
 * returns zero for any condition.
 *
 * @param <T>
 *            concentration type
 */
public class AnyRealDistribution<T> extends AbstractDistribution<T> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @SuppressFBWarnings(
        value = "SE_BAD_FIELD",
        justification = "All the implementations of RealDistribution also implement Serializable"
    )
    private final RealDistribution distribution;
    private Time next;

    /**
     * @param rng
     *            the {@link RandomGenerator}
     * @param distribution
     *            the distribution name (case insensitive). Must be mappable to an entity implementing {@link RealDistribution}
     * @param parameters
     *            the parameters for the distribution
     */
    public AnyRealDistribution(final RandomGenerator rng, final String distribution, final double... parameters) {
        this(Time.ZERO, rng, distribution, parameters);
    }

    /**
     * @param start
     *            the initial time
     * @param rng
     *            the {@link RandomGenerator}
     * @param distribution
     *            the distribution name (case insensitive). Must be mappable to an entity implementing {@link RealDistribution}
     * @param parameters
     *            the parameters for the distribution
     */
    public AnyRealDistribution(
            final Time start,
            final RandomGenerator rng,
            final String distribution,
            final double... parameters
    ) {
        this(start, RealDistributionUtil.makeRealDistribution(rng, distribution, parameters));
    }

    /**
     * @param distribution
     *            the {@link AnyRealDistribution} to use. To ensure
     *            reproducibility, such distribution must get created using the
     *            simulation {@link RandomGenerator}.
     */
    public AnyRealDistribution(final RealDistribution distribution) {
        this(Time.ZERO, distribution);
    }

    /**
     * @param start
     *            distribution start time
     * @param distribution
     *            the {@link AnyRealDistribution} to use. To ensure
     *            reproducibility, such distribution must get created using the
     *            simulation {@link RandomGenerator}.
     */
    public AnyRealDistribution(final Time start, final RealDistribution distribution) {
        super(start);
        this.distribution = distribution;
    }

    @Override
    public final double getRate() {
        return distribution.getNumericalMean();
    }

    /**
     * This method can be overridden to implement further controls.
     * Subclasses should still call super.updateStatus, though.
     * {@inheritDoc}
     */
    @Override
    protected void updateStatus(
            final Time currentTime,
            final boolean hasBeenExecuted,
            final double additionalParameter,
            final Environment<T, ?> environment
    ) {
        if (
            next == null
                || hasBeenExecuted
                || currentTime.compareTo(next) < 0
                || additionalParameter > 0 && getNextOccurence().isInfinite()
        ) {
            // New time generation necessary
            final var step = distribution.sample();
            if (step < 0) {
                throw new IllegalStateException(distribution + " generated a negative delta time: " + step);
            }
            next = new DoubleTime(currentTime.toDouble() + step);
        }
        if (additionalParameter == 0) {
            // The execution is blocked
            setNextOccurrence(Time.INFINITY);
        } else {
            setNextOccurrence(next);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractDistribution<T> cloneOnNewNode(final Node<T> destination, final Time currentTime) {
        return new AnyRealDistribution<>(currentTime, distribution);
    }

    /**
     * @return the interanl {@link RealDistribution}
     */
    protected final RealDistribution getDistribution() {
        return distribution;
    }
}
