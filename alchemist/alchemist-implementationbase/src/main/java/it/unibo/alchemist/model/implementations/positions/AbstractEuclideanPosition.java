/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/

/**
 * 
 */
package it.unibo.alchemist.model.implementations.positions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.MathArrays;
import org.danilopianini.util.Hashes;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * N-dimensional Euclidean position.
 *
 * @param <P>
 *            actual type
 */
public abstract class AbstractEuclideanPosition<P extends AbstractEuclideanPosition<P>> implements Position<P> {

    /**
     * 
     */
    private static final long serialVersionUID = 2993200108153260352L;
    private final double[] c;
    private int hash;
    private String stringCache;

    /**
     * @param copy
     *            true if it is unsafe to store the array as-is
     * @param coord
     *            the array of coordinates
     */
    protected AbstractEuclideanPosition(final boolean copy, final double... coord) { // NOPMD: array stored directly by purpose
        if (copy) {
            c = Arrays.copyOf(coord, coord.length);
        } else {
            c = coord;
        }
        org.apache.commons.math3.util.MathUtils.checkFinite(c);
    }

    @Override
    public final List<? extends P> boundingBox(final double range) {
        final List<P> box = new ArrayList<>(getDimensions());
        for (int i = 0; i < getDimensions(); i++) {
            final double[] coords = new double[c.length];
            /*
             * Canonical base
             */
            for (int j = 0; j < coords.length; j++) {
                coords[j] = c[j] + (i == j ? -range : range);
            }
            box.add(unsafeConstructor(coords));
        }
        return box;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() == getClass()) {
            return samePosition((P) o);
        } else {
            return false;
        }
    }

    @Override
    public final double[] getCartesianCoordinates() {
        return Arrays.copyOf(c, c.length);
    }

    @Override
    public final double getCoordinate(final int dim) {
        if (dim < 0 || dim >= c.length) {
            throw new IllegalArgumentException(dim + "is not an allowed dimension, only values between 0 and " + (c.length - 1) + "are allowed.");
        }
        return c[dim];
    }

    @Override
    public final int getDimensions() {
        return c.length;
    }

    @Override
    public final double getDistanceTo(final P p) {
        final double[] coord = p.getCartesianCoordinates();
        if (c.length == coord.length) {
            return MathArrays.distance(c, coord);
        } else {
            throw new UncomparableDistancesException(this, p);
        }
    }

    @Override
    public final int hashCode() {
        if (hash == 0) {
            hash = Hashes.hash32(c);
        }
        return hash;
    }

    /**
     * @param o
     *            the position to compare with
     * @return true if the two positions are the the same
     */
    public boolean samePosition(final P o) {
        final double[] p = o.getCartesianCoordinates();
        return Arrays.equals(c, p);
    }

    /**
     * Currently a print of the array of coordinates.
     */
    @Override
    public String toString() {
        if (stringCache == null) {
            stringCache = Arrays.toString(c);
        }
        return stringCache;
    }

    @Override
    public final P plus(final P other) {
        return unsafeConstructor(MathArrays.ebeAdd(c, other.getCartesianCoordinates()));
    }

    @Override
    public final P minus(final P other) {
        return unsafeConstructor(MathArrays.ebeSubtract(c, other.getCartesianCoordinates()));
    }

    /**
     * Calls an internal constructor of subclasses that provides a way to instance a
     * new position given its coordinates.
     * 
     * @param coord
     *            the coordinates
     * @return a new position (with correct subtype)
     */
    protected abstract P unsafeConstructor(double[] coord);

}
