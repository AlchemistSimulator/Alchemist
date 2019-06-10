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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * This class measures distances as integers. Suitable for bidimensional
 * discrete environments. The distance between two nodes is computed as
 * Manhattan distance.
 */
public final class Manhattan2DPosition implements Position2D<Manhattan2DPosition> {

    private static final int MASK = 0x0000FFFF;
    private static final long serialVersionUID = 4773955346963361299L;
    private static final byte SHIFT = 16;
    private final int xCoord, yCoord, hash;

    /**
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     */
    public Manhattan2DPosition(final int x, final int y) {
        this.xCoord = x;
        this.yCoord = y;
        hash = (x & MASK) << SHIFT | (y & MASK);
    }

    @Override
    public Manhattan2DPosition plus(final Manhattan2DPosition other) {
        return new Manhattan2DPosition(xCoord + other.xCoord, yCoord + other.yCoord);
    }

    @Override
    public List<Manhattan2DPosition> boundingBox(final double r) {
        final int range = (int) r;
        final Manhattan2DPosition bl = new Manhattan2DPosition(xCoord - range, yCoord - range);
        final Manhattan2DPosition ur = new Manhattan2DPosition(xCoord + range, yCoord + range);
        return Lists.newArrayList(bl, ur);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Manhattan2DPosition) {
            final Manhattan2DPosition d = (Manhattan2DPosition) o;
            return xCoord == d.xCoord && yCoord == d.yCoord;
        }
        return false;
    }

    @Override
    public double[] getCartesianCoordinates() {
        return new double[] { xCoord, yCoord };
    }

    @Override
    @Deprecated
    public double getCoordinate(final int dim) {
        if (dim == 0) {
            return xCoord;
        }
        if (dim == 1) {
            return yCoord;
        }
        throw new IllegalArgumentException(dim + " is not a valid dimension. Only 0 and 1 can be used.");
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public double getDistanceTo(final Manhattan2DPosition p) {
        return Math.abs(xCoord - p.xCoord) + Math.abs(yCoord - p.yCoord);
    }

    @Override
    public double getX() {
        return xCoord;
    }

    @Override
    public double getY() {
        return yCoord;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public Manhattan2DPosition minus(final Manhattan2DPosition other) {
        return new Manhattan2DPosition(xCoord - other.xCoord, yCoord - other.yCoord);
    }

    @Override
    public String toString() {
        return "[" + xCoord + "," + yCoord + "]";
    }

}
