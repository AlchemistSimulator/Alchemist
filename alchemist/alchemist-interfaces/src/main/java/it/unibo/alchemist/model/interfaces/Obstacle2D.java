/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.awt.Shape;
import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.geometry.Vector2D;

/**
 * An obstacle in a bidimensional space.
 *
 * @param <V> the position type for the space in which this obstacle is placed.
 */
public interface Obstacle2D<V extends Vector2D<V>> extends Serializable, Shape, Obstacle<V> {

    /**
     * @return the id for this obstacle
     */
    int getId();

    /**
     * Given a vector (represented as a starting point and an end point) and a
     * rectangle, computes the intersection point between the vector and the
     * rectangle nearest to the vector's starting point.
     * 
     * @param startx
     *            start x coordinate of the vector
     * @param starty
     *            start y coordinate of the vector
     * @param endx
     *            end x coordinate of the vector
     * @param endy
     *            end y coordinate of the vector
     * @return the intersection point between the vector and the rectangle
     *         nearest to the vector's starting point
     */
    double[] nearestIntersection(double startx, double starty, double endx, double endy);
}
