/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.layers;

import it.unibo.alchemist.model.Position2D;
import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.Layer;

/**
 * A {@link Layer} representing a linear distribution in space of a molecule.
 *
 * @param <P> {@link Position2D} type
 */
public final class BiomolGradientLayer<P extends Position2D<P>> implements Layer<Double, P> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final double a;
    private final double b;
    private final double c;
    private final double steep;

    /**
     * Initialize a gradient layer which grows in concentration proportionally in space.
     * 
     * @param directionX x coordinate of the vector representing the direction in which the gradient grows
     * @param directionY y coordinate of the vector representing the direction in which the gradient grows
     * @param unitVariation unit variation of the gradient
     * @param offset minimum value of concentration reached by this spatial distribution
     */
    public BiomolGradientLayer(
            final double directionX,
            final double directionY,
            final double unitVariation,
            final double offset
    ) {
        final double dirModule = FastMath.sqrt(FastMath.pow(directionX, 2) + FastMath.pow(directionY, 2));
        steep = unitVariation;
        // versor coordinates
        assert dirModule != 0;
        final double vx = directionX / dirModule;
        final double vy = directionY / dirModule;
        // initialize the parameters of plan representing the gradient.
        c = offset;
        a = unitVariation * vx;
        b = unitVariation * vy;
    }

    /**
     * Initialize a gradient layer which grows in concentration proportionally in space.
     * 
     * @param direction the {@link Position2D} representing the direction
     *                  in which the gradient grows (here the positions is considered as a vector)
     * @param unitVariation unit variation of the gradient
     * @param offset minimum value of concentration reached by this spatial distribution
     */
    public BiomolGradientLayer(final P direction, final double unitVariation, final double offset) {
        this(direction.getX(), direction.getY(), unitVariation, offset);
    }

    @Override
    public Double getValue(final P p) {
        return p.getX() * a + p.getY() * b + c;
    }

    @Override
    public String toString() {
        return "Layer representing a gradient of the molecule. "
                + "The equation describing this gradient is: concentration = " + a 
                + "x + " + b + "y + " + c;
    }

    /**
     * 
     * @return the parameters describing this spatial distribution, that's actually a plain. So the
     * {@link java.lang.reflect.Array} a returned by this method contains the parameters of that plain
     * (concentration = a[0] * x + a[1] * y + a[2])
     */
    public double[] getParameters() {
        return new double[]{a, b, c};
    }

    /**
     * 
     * @return the steepness of the gradient
     */
    public double getSteep() {
        return steep;
    }
}
