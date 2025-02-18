/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions;

import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.times.DoubleTime;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serial;

/**
 * A {@link DiracComb} whose rate is determined (uniformly) randomly within the provided bounds.
 *
 * @param <T> concentration type
 */
public class RandomDiracComb<T> extends DiracComb<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param rng
     *            the {@link RandomGenerator}
     * @param start
     *            the start {@link Time}
     * @param minRate
     *            the minimum rate
     * @param maxRate
     *            the maximum rate
     */
    public RandomDiracComb(final RandomGenerator rng, final Time start, final double minRate, final double maxRate) {
        super(start, minRate + (maxRate - minRate) * rng.nextDouble());
        if (minRate > maxRate || maxRate <= 0 || minRate <= 0) {
            throw new IllegalArgumentException("Invalid rate values: {min=" + minRate + ", max=" + maxRate + "}.");
        }
    }

    /**
     * @param rng
     *            the {@link RandomGenerator}
     * @param minRate
     *            the minimum rate
     * @param maxRate
     *            the maximum rate
     */
    public RandomDiracComb(final RandomGenerator rng, final double minRate, final double maxRate) {
        this(rng, new DoubleTime(minRate * rng.nextDouble()), minRate, maxRate);
        if (minRate > maxRate || maxRate <= 0) {
            throw new IllegalArgumentException("Invalid rate values: {min=" + minRate + ", max=" + maxRate + "}.");
        }
    }
}
