/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.layers;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * A {@link Layer} representing a linear distribution in space of a molecule.
 *
 */
public final class BiomolGradientLayer<P extends Position<? extends P>> implements Layer<Double, P> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final double a;
    private final double b;
    private final double c;
    private final double steep;

    /**
     * Initialize a gradient layer which grows in concentration proportionaly in space. 
     * 
     * @param dirx x coordinate of the vector representing the direction in which the gradient grows
     * @param diry y coordinate of the vector representing the direction in which the gradient grows
     * @param unitVariation unit variation of the gradient
     * @param offset minimum value of concentration reached by this spatial distribution
     */
    public BiomolGradientLayer(final double dirx, final double diry, final double unitVariation, final double offset) {
        final double dirModule = FastMath.sqrt(FastMath.pow(dirx, 2) + FastMath.pow(diry, 2));
        steep = unitVariation;
        // versor coordinates
        assert dirModule != 0;
        final double vx = dirx / dirModule;
        final double vy = diry / dirModule;
        // initialize the parameters of plan representing the gradient.
        c = offset;
        a = unitVariation * vx;
        b = unitVariation * vy;
    }

    /**
     * Initialize a gradient layer which grows in concentration proportionaly in space. 
     * 
     * @param direction the {@link Position} representing the direction in which the gradient grows (here the positions is considered as a vector)
     * @param unitVariation unit variation of the gradient
     * @param offset minimum value of concentration reached by this spatial distribution
     */
    public BiomolGradientLayer(final P direction, final double unitVariation, final double offset) {
        this(direction.getCoordinate(0), direction.getCoordinate(1), unitVariation, offset);
    }

    @Override
    public Double getValue(final P p) {
        final double[] cord = p.getCartesianCoordinates();
        return (cord[0] * a) + (cord[1] * b) + c;
    }

    @Override
    public String toString() {
        return "Layer representing a gradient of the molecule. "
                + "The equation describing this gradient is: concentration = " + a 
                + "x + " + b + "y + " + c;
    }

    /**
     * 
     * @return the parameters describing this spatial distribution, that's actually a plain. So the {@link Array} a returned by this method contains the parameters of that plain ( concentration = a[0] * x + a[1] * y + a[2])
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
