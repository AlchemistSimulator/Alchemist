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

import org.apache.commons.math3.util.Pair;

/**
 */
public interface Obstacle2D extends Serializable, Shape {

    /**
     * @return the id for this obstacle
     */
    int getId();

    /**
     * Given a vector (starting point and end point) representing a requested
     * move, this method computes a new end point, representing a cut version of
     * the initial vector, modified in such a way that the end point is outside the obstacle.
     * 
     * @param sx
     *            start x coordinate of the vector
     * @param sy
     *            start y coordinate of the vector
     * @param ex
     *            end x coordinate of the vector
     * @param ey
     *            end y coordinate of the vector
     * @return the intersection point between the vector and the rectangle
     *         nearest to the vector's starting point
     */
    Pair<Double, Double> next(double sx, double sy, double ex, double ey);

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
