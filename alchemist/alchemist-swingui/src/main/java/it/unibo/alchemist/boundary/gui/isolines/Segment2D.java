/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines;

/**
 * A basic 2-dimensional segment.
 */
public interface Segment2D {

    /**
     * @return x coordinate of point 1
     */
    Number getX1();

    /**
     * @return y coordinate of point 1
     */
    Number getY1();

    /**
     * @return x coordinate of point 2
     */
    Number getX2();

    /**
     * @return y coordinate of point 2
     */
    Number getY2();

}
