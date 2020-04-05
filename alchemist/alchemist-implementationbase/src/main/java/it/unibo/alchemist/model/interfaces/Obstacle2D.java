/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.awt.Shape;

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;

/**
 * An obstacle in a bidimensional space.
 */
public interface Obstacle2D extends Obstacle<Euclidean2DPosition>, Shape {

    /**
     * Given a vector (represented as a starting point and an end point), computes
     * the intersection point between the vector and the obstacle nearest to the
     * vector's starting point.
     *
     * @param start
     *          starting point of the vector
     * @param end
     *          ending point of the vector
     * @return the intersection point between the vector and the rectangle
     *         nearest to the vector's starting point
     */
    Euclidean2DPosition nearestIntersection(Euclidean2DPosition start, Euclidean2DPosition end);
}
