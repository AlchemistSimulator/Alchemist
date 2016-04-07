/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors.utils;

import gnu.trove.TDoubleCollection;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;

/**
 *
 */
public enum Aggregator {
    /**
     * 
     */
    MEAN(new Mean()), SUM(new Sum()), NONE(null),
    /**
     * Returns the <a href="http://www.xycoon.com/geometric_mean.htm">
     * geometric mean </a> of the available values.
     * <p>
     * Uses a {@link SumOfLogs} instance to compute sum of logs and returns
     * <code> exp( 1/n  (sum of logs) ).</code> Therefore,
     * </p>
     * <ul>
     * <li>If any of values are < 0, the result is <code>NaN.</code></li>
     * <li>If all values are non-negative and less than
     * <code>Double.POSITIVE_INFINITY</code>, but at least one value is 0,
     * the result is <code>0.</code></li>
     * <li>If both <code>Double.POSITIVE_INFINITY</code> and
     * <code>Double.NEGATIVE_INFINITY</code> are among the values, the
     * result is <code>NaN.</code></li>
     * </ul>
     * 
     * @see GeometricMean
     */
    GEOMETRIC_MEAN(new GeometricMean()),
    /**
     * Computes the Kurtosis of the available values.
     * <p>
     * We use the following (unbiased) formula to define kurtosis:
     * </p>
     * <p>
     * kurtosis = { [n(n+1) / (n -1)(n - 2)(n-3)] sum[(x_i - mean)^4] /
     * std^4 } - [3(n-1)^2 / (n-2)(n-3)]
     * </p>
     * <p>
     * where n is the number of values, mean is the {@link Mean} and std is
     * the {@link StandardDeviation}
     * </p>
     * <p>
     * Note that this statistic is undefined for n < 4.
     * <code>Double.Nan</code> is returned when there is not sufficient data
     * to compute the statistic.
     * </p>
     * 
     * @see Kurtosis
     */
    KURTOSIS(new Kurtosis()),
    /**
     * @see Max
     */
    MAX(new Max()),
    /**
     * @see Median
     */
    MEDIAN(new Median()),
    /**
     * @see Percentile
     */
    PERCENTILE50(new Percentile(50)), PERCENTILE75(new Percentile(75)), PERCENTILE90(new Percentile(90)), PERCENTILE95(new Percentile(95)), PERCENTILE99(new Percentile(99)),
    /**
     * @see Product
     */
    PRODUCT(new Product()),
    /**
     * Computes a statistic related to the Second Central Moment.
     * Specifically, what is computed is the sum of squared deviations from
     * the sample mean.
     * <p>
     * The following recursive updating formula is used:
     * </p>
     * <p>
     * Let
     * <ul>
     * <li>dev = (current obs - previous mean)</li>
     * <li>n = number of observations (including current obs)</li>
     * </ul>
     * Then
     * </p>
     * <p>
     * new value = old value + dev^2 * (n -1) / n.
     * </p>
     * <p>
     * Returns <code>Double.NaN</code> if no data values have been added and
     * returns <code>0</code> if there is just one value in the data set.
     * </p>
     * 
     * @see SecondMoment
     */
    SECOND_MOMENT(new SecondMoment()),
    /**
     * @see Skewness
     */
    SKEWNESS(new Skewness()),
    /**
     * @see StandardDeviation
     */
    STANDARD_DEVIATION(new StandardDeviation()),
    /**
     * @see SumOfLogs
     */
    SUM_OF_LOGS(new SumOfLogs()),
    /**
     * @see SumOfSquares
     */
    SUM_OF_SQUARES(new SumOfSquares()),
    /**
     * @see Min
     */
    MIN(new Min()),
    /**
     * @see Variance
     */
    VARIANCE(new Variance());

    private final UnivariateStatistic stat;
    private final Semaphore mutex = new Semaphore(1);

    Aggregator(final UnivariateStatistic s) {
        stat = s;
    }

    /**
     * @param data
     *            the data to aggregate
     * @return the aggregated version
     */
    public double[] aggregate(final List<TDoubleCollection> data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        if (!data.isEmpty()) {
            if (this.equals(NONE)) {
                final TDoubleList result = new TDoubleArrayList();
                for (final TDoubleCollection list : data) {
                    result.addAll(list);
                }
                return result.toArray();
            }
            double[] res = new double[data.size()];
            for (int i = 0; i < res.length; i++) {
                /*
                 * Apache Commons Math3 is documented to be thread-unsafe.
                 */
                mutex.acquireUninterruptibly();
                res[i] = stat.evaluate(data.get(i).toArray());
                mutex.release();
            }
            return res;
        }
        return new double[0];
    }

}
