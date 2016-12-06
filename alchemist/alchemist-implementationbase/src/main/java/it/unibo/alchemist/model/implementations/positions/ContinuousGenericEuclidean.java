/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.positions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.MathArrays;
import org.danilopianini.lang.HashUtils;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.model.interfaces.Position;

/**
 */
public class ContinuousGenericEuclidean implements Position {

    /**
     * 
     */
    private static final long serialVersionUID = 2993200108153260352L;
    private final double[] c;
    private int hash;
    private String stringCache;

    /**
     * 
     * @param coord
     *            the coordinates
     */
    public ContinuousGenericEuclidean(final double... coord) {
        this(true, coord);
    }

    private ContinuousGenericEuclidean(final boolean copy, final double... coord) {
        if (copy) {
            c = Arrays.copyOf(coord, coord.length);
        } else {
            c = coord;
        }
        org.apache.commons.math3.util.MathUtils.checkFinite(c);
    }

    @Override
    public List<Position> buildBoundingBox(final double range) {
        return IntStream.range(0, getDimensions()).parallel()
            .mapToObj(i -> {
                final double[] coords = new double[c.length];
                /*
                 * Canonical base: always sum the range, but
                 */
                for (int j = 0; j < coords.length; j++) {
                    coords[j] = c[j] + (i == j ? -range : range);
                }
                return new ContinuousGenericEuclidean(false, coords);
            })
            .collect(Collectors.toList());
    }

    @Override
    public int compareTo(final Position o) {
        if (c.length < o.getDimensions()) {
            return -1;
        }
        if (c.length > o.getDimensions()) {
            return 1;
        }
        final double[] pos = o.getCartesianCoordinates();
        for (int i = 0; i < c.length; i++) {
            if (c[i] < pos[i]) {
                return -1;
            }
            if (c[i] > pos[i]) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Position) {
            return samePosition((Position) o);
        } else {
            return false;
        }
    }

    @Override
    public double[] getCartesianCoordinates() {
        return Arrays.copyOf(c, c.length);
    }

    @Override
    public double getCoordinate(final int dim) {
        if (dim < 0 || dim >= c.length) {
            throw new IllegalArgumentException(dim + "is not an allowed dimension, only values between 0 and " + c.length + "are allowed.");
        }
        return c[dim];
    }

    @Override
    public int getDimensions() {
        return c.length;
    }

    @Override
    public double getDistanceTo(final Position p) {
        final double[] coord = p.getCartesianCoordinates();
        if (c.length == coord.length) {
            return MathArrays.distance(c, coord);
        } else {
            throw new UncomparableDistancesException(this, p);
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = HashUtils.hash32(c);
        }
        return hash;
    }

    /**
     * @param o
     *            the position to compare with
     * @return true if the two positions are the the same
     */
    public boolean samePosition(final Position o) {
        final double[] p = o.getCartesianCoordinates();
        return Arrays.equals(c, p);
    }

    @Override
    public String toString() {
        if (stringCache == null) {
            stringCache = Arrays.toString(c);
        }
        return stringCache;
    }

    @Override
    public Position add(final Position other) {
        return new ContinuousGenericEuclidean(false, MathArrays.ebeAdd(c, other.getCartesianCoordinates()));
    }

    @Override
    public Position subtract(final Position other) {
        return new ContinuousGenericEuclidean(false, MathArrays.ebeSubtract(c, other.getCartesianCoordinates()));
    }

}
