/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.positions;

import it.unibo.alchemist.model.interfaces.Position;
import org.apache.commons.math3.util.MathArrays;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * N-dimensional position.
 *
 * @param <P>
 *            actual type
 */
public abstract class AbstractPosition<P extends Position<P>> implements Position<P> {

    private static final long serialVersionUID = 1L;
    private final @Nonnull double[] c;
    private int hash;
    private String stringCache;

    /**
     * @param copy
     *            true if it is unsafe to store the array as-is
     * @param coordinates
     *            the array of coordinates
     */
    protected AbstractPosition(final boolean copy, @Nonnull final double... coordinates) { // NOPMD
        // array stored directly by purpose here
        if (copy) {
            c = Arrays.copyOf(coordinates, coordinates.length);
        } else {
            c = coordinates;
        }
        org.apache.commons.math3.util.MathUtils.checkFinite(c);
    }

    @Override
    public final List<P> boundingBox(final double range) {
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

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() == getClass()) {
            return Arrays.equals(c, ((AbstractPosition<?>) o).c);
        } else {
            return false;
        }
    }

    @Override
    @Nonnull
    public final double[] getCoordinates() {
        return Arrays.copyOf(c, c.length);
    }

    @Override
    public final double getCoordinate(final int dim) {
        if (dim < 0 || dim >= c.length) {
            throw new IllegalArgumentException(dim + "is not an allowed dimension, only values between 0 and "
                    + (c.length - 1) + "are allowed.");
        }
        return c[dim];
    }

    @Override
    public final int getDimensions() {
        return c.length;
    }

    @Override
    public final double distanceTo(@Nonnull final P other) {
        return MathArrays.distance(c, ((AbstractPosition<P>) other).c);
    }

    @Override
    public final int hashCode() {
        if (hash == 0) {
            hash = Arrays.hashCode(c);
        }
        return hash;
    }

    /**
     * Prints the coordinates.
     */
    @Override
    public String toString() {
        if (stringCache == null) {
            stringCache = Arrays.toString(c);
        }
        return stringCache;
    }

    private @Nonnull double[] extractInternalRepresentation(final @Nonnull P position) {
        return ((AbstractPosition<P>) Objects.requireNonNull(position)).c;
    }

    /**
     * Same as {@link #plus(double[])}, with the internal representation of other.
     *
     * @param other the other position
     * @return a new position with the coordinates summed with the other
     */
    @Nonnull
    public final P plus(@Nonnull final P other) {
        return plus(extractInternalRepresentation(other));
    }

    @Nonnull
    @Override
    public final P plus(@Nonnull final double[] other) {
        return unsafeConstructor(MathArrays.ebeAdd(c, other));
    }

    /**
     * Same as {@link #minus(double[])}, with the internal representation of other.
     *
     * @param other the other position
     * @return a new position with the coordinates summed with the other
     */
    @Nonnull
    public final P minus(@Nonnull final P other) {
        return minus(extractInternalRepresentation(other));
    }

    @Nonnull
    @Override
    public final P minus(@Nonnull final double[] other) {
        return unsafeConstructor(MathArrays.ebeSubtract(c, other));
    }

    /**
     * Multiplication by a number.
     *
     * @param other the number
     * @return every element times the number
     */
    @Nonnull
    public final P times(final double other) {
        return unsafelyRun(it -> it * other);
    }

    /**
     * Division by a number.
     *
     * @param other the number
     * @return every element divided by the number
     */
    @Nonnull
    public final P div(final double other) {
        return unsafelyRun(it -> it / other);
    }

    private P unsafelyRun(final DoubleUnaryOperator operation) {
        final double[] result = Arrays.copyOf(c, c.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = operation.applyAsDouble(result[i]);
        }
        return unsafeConstructor(result);
    }

    /**
     * Calls an internal constructor of subclasses that provides a way to instance a
     * new position given its coordinates.
     * 
     * @param coordinates
     *            the coordinates
     * @return a new position (with correct subtype)
     */
    protected abstract P unsafeConstructor(double[] coordinates);

}
