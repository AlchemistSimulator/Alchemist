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
     * @param centerX x coordinate of the circle center
     * @param centerY y coordinate of the circle center
     * @param radius the circle radius
     */
    public Circle(final double centerX, final double centerY, final double radius) {
        super(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
        if (radius < 0) {
            throw new IllegalArgumentException("Circle radius must be positive (got: " + radius + ")");
        }
    }

}
