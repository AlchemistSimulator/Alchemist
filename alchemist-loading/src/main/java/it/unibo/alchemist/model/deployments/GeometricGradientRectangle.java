/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.deployments;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position;

import javax.annotation.Nonnull;

/**
 * Distributes nodes geometrically within a rectangular shape.
 *
 * @param <P> position type
 */
public final class GeometricGradientRectangle<P extends Position<? extends P>> extends Rectangle<P> {

    private final ExponentialDistribution exp;
    private final double bound, size;
    private final int steps;
    private final boolean continuous, horizontal, increasing;

    /**
     * Use this constructor to displace multiple groups of devices with
     * exponentially varied density along an axis.
     * 
     * @param environment
     *            {@link Environment}
     * @param randomGenerator
     *            {@link RandomGenerator}
     * @param nodes
     *            the number of nodes to displace
     * @param x
     *            start x position
     * @param y
     *            start y position
     * @param sizex
     *            width
     * @param sizey
     *            height
     * @param lambda
     *            the lambda parameter of the exponential. The actual lambda is
     *            computed by multiplying this value with the dimension chosen
     *            for the exponential distribution
     * @param steps
     *            number of discrete groups. One falls back to uniform
     *            distribution, very large values approximate a continuous
     *            exponential distribution
     * @param horizontal
     *            true if the exponential axis is horizontal
     * @param increasing
     *            true if device density should increase with the desired axis
     */
    public GeometricGradientRectangle(
        final RandomGenerator randomGenerator,
        final Environment<?, P> environment,
        final int nodes,
        final double x,
        final double y,
        final double sizex,
        final double sizey,
        final double lambda,
        final int steps,
        final boolean horizontal,
        final boolean increasing
    ) {
        this(randomGenerator, environment, nodes, x, y, sizex, sizey, lambda, false, steps, horizontal, increasing);
    }

    /**
     * Use this constructor to displace devices with an exponentially varied
     * density along an axis.
     * 
     * @param environment
     *            {@link Environment}
     * @param randomGenerator
     *            {@link RandomGenerator}
     * @param nodes
     *            the number of nodes to displace
     * @param x
     *            start x position
     * @param y
     *            start y position
     * @param sizex
     *            width
     * @param sizey
     *            height
     * @param lambda
     *            the lambda parameter of the exponential. The actual lambda is
     *            computed by multiplying this value with the dimension chosen
     *            for the exponential distribution
     * @param horizontal
     *            true if the exponential axis is horizontal
     * @param increasing
     *            true if device density should increase with the desired axis
     */
    public GeometricGradientRectangle(
        final RandomGenerator randomGenerator,
        final Environment<?, P> environment,
        final int nodes,
        final double x,
        final double y,
        final double sizex,
        final double sizey,
        final double lambda,
        final boolean horizontal,
        final boolean increasing
    ) {
        this(randomGenerator, environment, nodes, x, y, sizex, sizey, lambda, true, Integer.MIN_VALUE, horizontal, increasing);
    }

    private GeometricGradientRectangle(
        final RandomGenerator randomGenerator,
        final Environment<?, P> environment,
        final int nodes,
        final double x,
        final double y,
        final double sizex,
        final double sizey,
        final double lambda,
        final boolean continuous,
        final int steps,
        final boolean horizontal,
        final boolean increasing
    ) {
        super(environment, randomGenerator, nodes, x, y, sizex, sizey);
        if (lambda <= 0 || lambda > 100) {
            throw new IllegalArgumentException("lambda must be in the (0, 100] interval.");
        }
        if (!continuous && steps < 1) {
            throw new IllegalArgumentException("The number o steps must be greater than 0");
        }
        this.steps = steps;
        this.continuous = continuous;
        this.horizontal = horizontal;
        this.increasing = increasing;
        size = FastMath.abs(horizontal ? sizex : sizey);
        exp = new ExponentialDistribution(randomGenerator, size * lambda);
        bound = exp.cumulativeProbability(size);
        /*
         * Determine the bound in terms of cumulative probability
         */
    }

    @Nonnull
    @Override
    protected P indexToPosition(final int i) {
        double exponential = increasing ? size - nextExpRandom() : nextExpRandom();
        if (!continuous) {
            final double groupSize = size / steps;
            final double groupId = Math.floor(exponential / groupSize);
            final double groupStart = groupId * groupSize;
            exponential = randomDouble(groupStart, groupStart + groupSize);
        }
        final double x = getX() + Math.signum(getWidth()) * (horizontal ? exponential : randomDouble(0, Math.abs(getWidth())));
        final double y = getY() + Math.signum(getHeight()) * (horizontal ? randomDouble(0, Math.abs(getHeight())) : exponential);
        return makePosition(x, y);
    }

    /**
     * @return a random value with the chosen exponential distribution
     */
    private double nextExpRandom() {
        return exp.inverseCumulativeProbability(randomDouble(0, bound));
    }
}
