/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.model.implementations.positions;

import it.unibo.alchemist.model.interfaces.Position2D;

/**
 * 
 */
public final class Euclidean2DPosition
        extends AbstractEuclideanPosition<Euclidean2DPosition>
        implements Position2D<Euclidean2DPosition> {

    private static final long serialVersionUID = 1042391992665398942L;

    private Euclidean2DPosition(final boolean copy, final double xp, final double yp) {
        super(copy, xp, yp);
    }

    /**
     * @param xp
     *            The X coordinate
     * @param yp
     *            The Y coordinate
     */
    public Euclidean2DPosition(final double xp, final double yp) {
        this(true, xp, yp);
    }

    /**
     * @param c an array of length 2 containing the coordinates
     */
    public Euclidean2DPosition(final double[] c) {
        super(true, c);
        if (c.length != 2) {
            throw new IllegalArgumentException("The array must have exactly two elements.");
        }
    }

    @Override
    public double getX() {
        return getCoordinate(0);
    }

    @Override
    public double getY() {
        return getCoordinate(1);
    }

    @Override
    protected Euclidean2DPosition unsafeConstructor(final double[] coordinates) {
        return new Euclidean2DPosition(false, coordinates[0], coordinates[1]);
    }

}
