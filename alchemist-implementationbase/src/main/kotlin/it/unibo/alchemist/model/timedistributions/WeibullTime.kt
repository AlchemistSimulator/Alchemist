package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.distribution.WeibullDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.special.Gamma
import org.apache.commons.math3.util.FastMath

/**
 * Weibull distributed events.
 *
 * @param <T> concentration type
 */
open class WeibullTime<T> private constructor(
    private val randomGenerator: RandomGenerator,
    private val backingDistribution: WeibullDistribution,
    private val offset: Double,
    start: Time,
) : AbstractDistribution<T?>(start) {

    /**
     * @param mean
     *            mean for this distribution
     * @param deviation
     *            standard deviation for this distribution
     * @param random
     *            {@link RandomGenerator} used internally
     */
    constructor(mean: Double, deviation: Double, random: RandomGenerator) : this(
        mean,
        deviation,
        DoubleTime(random.nextDouble() * mean),
        random,
    )

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
    constructor(mean: Double, deviation: Double, start: Time, random: RandomGenerator) : this(
        random,
        weibullFromMean(mean, deviation, random),
        0.0,
        start,
    )

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
    constructor(
        shapeParameter: Double,
        scaleParameter: Double,
        offsetParameter: Double,
        start: Time,
        random: RandomGenerator,
    ) : this(
        random,
        WeibullDistribution(random, shapeParameter, scaleParameter, PREFERRED_INVERSE_CUMULATIVE_ACCURACY),
        offsetParameter,
        start,
    )

    override fun updateStatus(currentTime: Time, executed: Boolean, param: Double, environment: Environment<T?, *>?) {
        if (executed) {
            this.setNextOccurrence(currentTime.plus(DoubleTime(1.0 / this.genSample())))
        }
    }

    /**
     * @return a sample from the distribution
     */
    protected fun genSample(): Double =
        backingDistribution.inverseCumulativeProbability(randomGenerator.nextDouble()) + this.offset

    /**
     * @return the mean for this distribution.
     */
    val mean: Double
        get() = backingDistribution.numericalMean + this.offset

    /**
     * @return the standard deviation for this distribution.
     */
    val deviation: Double
        get() = FastMath.sqrt(backingDistribution.numericalVariance)

    override fun getRate(): Double = this.mean

    override fun cloneOnNewNode(
        destination: Node<T?>,
        currentTime: Time,
    ): WeibullTime<T?> = WeibullTime(
        this.randomGenerator,
        this.backingDistribution,
        this.offset,
        currentTime,
    )

    protected companion object {
        private const val PREFERRED_INVERSE_CUMULATIVE_ACCURACY = 1.0E-9

        /**
         * Generates a {@link WeibullDistribution} given its mean and standard deviation.
         *
         * @param mean
         *            the mean
         * @param deviation
         *            the standard deviation
         * @param random
         *            the random generator
         * @return a new {@link WeibullDistribution}
         */
        protected fun weibullFromMean(mean: Double, deviation: Double, random: RandomGenerator?): WeibullDistribution {
            val t = FastMath.log(deviation * deviation / (mean * mean) + 1.0)
            var kmin = 0.0

            var kmax: Double
            kmax = 1.0
            while (Gamma.logGamma(1.0 + 2.0 * kmax) - 2.0 * Gamma.logGamma(1.0 + kmax) < t) {
                kmin = kmax
                kmax *= 2.0
            }

            var k: Double
            k = (kmin + kmax) / 2.0
            while (kmin < k && k < kmax) {
                if (Gamma.logGamma(1.0 + 2.0 * k) - 2.0 * Gamma.logGamma(1.0 + k) < t) {
                    kmin = k
                } else {
                    kmax = k
                }
                k = (kmin + kmax) / 2.0
            }

            val shapeParameter = 1.0 / k
            val scaleParameter = mean / FastMath.exp(Gamma.logGamma(1.0 + k))
            return WeibullDistribution(random, shapeParameter, scaleParameter, PREFERRED_INVERSE_CUMULATIVE_ACCURACY)
        }
    }
}
