/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.timedistributions;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.distribution.WeibullDistribution;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Weibull distributed events.
 * 
 * @param <T>
 */
public class WeibullTime<T> extends AbstractDistribution<T> {

    private static final long serialVersionUID = 5216987069271114818L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator rand;
    private final WeibullDistribution dist;
    private final double offset;

    /**
     * @param mean
     *            mean for this distribution
     * @param deviation
     *            standard deviation for this distribution
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public WeibullTime(final double mean, final double deviation, final RandomGenerator random) {
        this(mean, deviation, new DoubleTime(random.nextDouble() * mean), random);
    }

    /**
     * @param mean
     *            mean for this distribution
     * @param deviation
     *            standard deviation for this distribution
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public WeibullTime(final double mean, final double deviation, final Time start, final RandomGenerator random) {
        this(random, weibullFromMean(mean, deviation, random), 0, start);
    }

    /**
     * @param shapeParameter
     *            shape parameter for this distribution
     * @param scaleParameter
     *            shape parameter for this distribution
     * @param offsetParameter
     *            minimum possible time interval for this distribution
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public WeibullTime(final double shapeParameter, final double scaleParameter, final double offsetParameter,
            final Time start, final RandomGenerator random) {
        this(random, new WeibullDistribution(random, shapeParameter, scaleParameter,
                WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY), offsetParameter, start);
    }

    private WeibullTime(final RandomGenerator rand, final WeibullDistribution dist, final double offset, final Time start) {
        super(start);
        this.rand = rand;
        this.dist = dist;
        this.offset = offset;
    }

    @Override
    public void updateStatus(final Time curTime, final boolean executed, final double param, final Environment<T, ?> env) {
        if (executed) {
            setTau(curTime.sum(new DoubleTime(genSample())));
        }
    }

    /**
     * @return a sample from the distribution
     */
    protected double genSample() {
        return dist.inverseCumulativeProbability(rand.nextDouble()) + offset;
    }

    @Override
    @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
    public WeibullTime<T> clone() {
        return new WeibullTime<>(dist.getShape(), dist.getScale(), offset, getNextOccurence(), rand);
    }

    /**
     * @return the mean for this distribution.
     */
    public double getMean() {
        return dist.getNumericalMean() + offset;
    }

    /**
     * @return the standard deviation for this distribution.
     */
    public double getDeviation() {
        return FastMath.sqrt(dist.getNumericalVariance());
    }

    @Override
    public double getRate() {
        return getMean();
    }

    /**
     * Generates a {@link WeibullDistribution} given its mean and stdev.
     * 
     * @param mean
     *            the mean
     * @param deviation
     *            the stdev
     * @param random
     *            the random generator
     * @return a new {@link WeibullDistribution}
     */
    protected static WeibullDistribution weibullFromMean(final double mean, final double deviation, final RandomGenerator random) {
        final double t = FastMath.log((deviation * deviation) / (mean * mean) + 1);
        double kmin = 0, kmax = 1;
        while (Gamma.logGamma(1 + 2 * kmax) - 2 * Gamma.logGamma(1 + kmax) < t) {
            kmin = kmax;
            kmax *= 2;
        }
        double k = (kmin + kmax) / 2;
        while (kmin < k && k < kmax) {
            if (Gamma.logGamma(1 + 2 * k) - 2 * Gamma.logGamma(1 + k) < t) {
                kmin = k;
            } else {
                kmax = k;
            }
            k = (kmin + kmax) / 2;
        }
        final double shapeParameter = 1 / k;
        final double scaleParameter = mean / FastMath.exp(Gamma.logGamma(1 + k));
        return new WeibullDistribution(random, shapeParameter, scaleParameter, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @Override
    public WeibullTime<T> clone(final Time currentTime) {
        return new WeibullTime<>(rand, dist, offset, currentTime);
    }
}
