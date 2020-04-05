/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.awt.Shape;

import it.unibo.alchemist.model.interfaces.geometry.Vector2D;

/**
 * An obstacle in a bidimensional space.
 *
 * @param <V> the position type for the space in which this obstacle is placed.
 */
public interface Obstacle2D<V extends Vector2D<V>> extends Shape, Obstacle<V> {

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
    V nearestIntersection(V start, V end);
}
