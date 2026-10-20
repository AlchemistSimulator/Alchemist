/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.isolines.api;

/**
 * A basic 2-dimensional segment.
 */
public interface Segment2D {

    /**
     * Returns the x coordinate of the first point.
     *
     * @return x coordinate of point 1
     */
    Number getX1();

    /**
     * Returns the y coordinate of the first point.
     *
     * @return y coordinate of point 1
     */
    Number getY1();

    /**
     * Returns the x coordinate of the second point.
     *
     * @return x coordinate of point 2
     */
    Number getX2();

    /**
     * Returns the y coordinate of the second point.
     *
     * @return y coordinate of point 2
     */
    Number getY2();

}
