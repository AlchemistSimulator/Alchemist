/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.positions;

import it.unibo.alchemist.model.interfaces.geometry.Vector;

import javax.annotation.Nonnull;

/**
 * N-dimensional Euclidean position.
 *
 * @param <P>
 *            actual type
 */
public abstract class AbstractEuclideanPosition<P extends AbstractEuclideanPosition<P>>
    extends AbstractPosition<P> implements Vector<P> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param copy
     *            true if it is unsafe to store the array as-is
     * @param coordinates
     *            the array of coordinates
     */
    protected AbstractEuclideanPosition(final boolean copy, @Nonnull final double... coordinates) { // NOPMD
        // array stored directly by purpose
        super(copy, coordinates);
    }

    @Override
    public final double get(final int dimension) {
        return super.getCoordinate(dimension);
    }

}
