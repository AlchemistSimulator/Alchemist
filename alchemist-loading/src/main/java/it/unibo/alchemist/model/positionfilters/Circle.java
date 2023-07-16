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

import java.awt.geom.Ellipse2D;

/**
 * A circle.
 *
 * @param <P> Position type
 */
public class Circle<P extends Position2D<P>> extends Abstract2DShape<P> {

    /**
     * @param centerx x coordinate of the center of the circle
     * @param centery y coordinate of the center of the circle
     * @param radius the circle radius
     */
    public Circle(final double centerx, final double centery, final double radius) {
        super(new Ellipse2D.Double(centerx - radius, centery - radius, radius * 2, radius * 2));
        if (radius < 0) {
            throw new IllegalArgumentException("Circle radius must be positive (got: " + radius + ")");
        }
    }

}
