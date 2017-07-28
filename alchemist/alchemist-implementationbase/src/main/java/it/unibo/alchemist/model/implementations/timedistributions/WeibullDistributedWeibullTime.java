package it.unibo.alchemist.model.implementations.timedistributions;

import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Weibull distributed events, with different (Weibull distributed) mean.
 * 
 * @param <T>
 */
public class WeibullDistributedWeibullTime<T> extends WeibullTime<T> {

    private static final long serialVersionUID = 1L;

    /**
     * @param mean
     *            mean time interval across the network
     * @param deviceDeviation
     *            standard deviation of time intervals in each device
     * @param networkDeviation
     *            standard deviation of time intervals across devices
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public WeibullDistributedWeibullTime(final double mean, final double deviceDeviation, final double networkDeviation, final RandomGenerator random) {
        this(mean, deviceDeviation, networkDeviation, new DoubleTime(random.nextDouble() * mean), random);
    }

    /**
     * @param mean
     *            mean time interval across the network
     * @param deviceDeviation
     *            standard deviation of time intervals in each device
     * @param networkDeviation
     *            standard deviation of time intervals across devices
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public WeibullDistributedWeibullTime(final double mean, final double deviceDeviation, final double networkDeviation, final Time start, final RandomGenerator random) {
        this(mean, deviceDeviation, networkDeviation, 0, start, random);
    }

    /**
     * @param mean
     *            mean time interval across the network
     * @param deviceDeviation
     *            standard deviation of time intervals in each device
     * @param networkDeviation
     *            standard deviation of time intervals across devices
     * @param deviationDeviation
     *            standard deviation of standard deviations across devices
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public WeibullDistributedWeibullTime(final double mean, final double deviceDeviation, final double networkDeviation, final double deviationDeviation, final Time start, final RandomGenerator random) {
        super(weibullValue(mean, networkDeviation, random), weibullValue(deviceDeviation, deviationDeviation, random), start, random);
    }

    private static double weibullValue(final double mean, final double deviation, final RandomGenerator random) {
        if (deviation > 0) {
            final WeibullDistribution dist = weibullFromMean(mean, deviation, random);
            return dist.inverseCumulativeProbability(random.nextDouble());
        }
        return mean;
    }
}
