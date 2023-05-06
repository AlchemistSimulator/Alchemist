/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.positionfilters;

import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.model.PositionBasedFilter;

/**
 * A bidimensional Alchemist {@link PositionBasedFilter} that relies on AWT {@link java.awt.Shape}.
 *
 * @param <P> position type
 */
public abstract class Abstract2DShape<P extends Position2D<P>> implements PositionBasedFilter<P> {

    private final java.awt.Shape shape;

    /**
     * @param shape any Java AWT {@link java.awt.Shape}
     */
    protected Abstract2DShape(final java.awt.Shape shape) {
        this.shape = shape;
    }

    @Override
    public final boolean contains(final P position) {
        if (position.getDimensions() != 2) {
            throw new IllegalArgumentException("Only bidimensional positions are accepted by this "
                    + getClass().getSimpleName());
        }
        return shape.contains(position.getX(), position.getY());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return shape.toString();
    }

}
