/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.shapes;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * A bidimensional Alchemist {@link Shape} that relies on AWT {@link java.awt.Shape}.
 */
public abstract class Abstract2DShape implements Shape {

    private final java.awt.Shape shape;

    /**
     * @param shape any Java AWT {@link java.awt.Shape}
     */
    protected Abstract2DShape(final java.awt.Shape shape) {
        this.shape = shape;
    }

    @Override
    public final boolean contains(final Position position) {
        if (position.getDimensions() != 2) {
            throw new IllegalArgumentException("Only bidimensional positions are accepted by this "
                    + getClass().getSimpleName());
        }
        return shape.contains(position.getCoordinate(0), position.getCoordinate(1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return shape.toString();
    }

}
