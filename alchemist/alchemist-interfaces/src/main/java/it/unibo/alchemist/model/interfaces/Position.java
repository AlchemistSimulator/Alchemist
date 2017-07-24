/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;
import java.util.List;

/**
 * An interface to represent a generic coordinates system.
 * 
 */
public interface Position extends Serializable {

    /**
     * Given a range, produces N coordinates, representing the N opposite
     * vertices of the hypercube having the current coordinate as center and
     * circumscribing the N-sphere defined by the range. In the case of two
     * dimensional coordinates, it must return the opposite vertices of the
     * square circumscribing the circle with center in this position and radius
     * range.
     * 
     * @param range
     *            the radius of the hypersphere
     * @return the vertices of the circumscribed hypercube
     */
    List<Position> buildBoundingBox(double range);

    /**
     * Allows to get the position as a Number array.
     * 
     * @return an array of size getDimensions() where each element represents a
     *         coordinate.
     */
    double[] getCartesianCoordinates();

    /**
     * Allows to access the value of a coordinate.
     * 
     * @param dim
     *            the dimension. E.g., in a 2-dimensional implementation, 0
     *            could be the X-axis and 1 the Y-axis
     * @return the coordinate value
     */
    double getCoordinate(int dim);

    /**
     * @return the number of dimensions of this {@link Position}.
     */
    int getDimensions();

    /**
     * Computes the distance between this position and another compatible
     * position.
     * 
     * @param p
     *            the position you want to know the distance to
     * @return the distance between this and p
     */
    double getDistanceTo(Position p);

    /**
     * Considers both positions as vectors, and sums them.
     * 
     * @param other the other position
     * @return a new {@link Position} that is the sum of the two.
     */
    Position add(Position other);

    /**
     * Considers both positions as vectors, and returns the difference between this position and the passed one.
     * 
     * @param other the other position
     * @return a new {@link Position} that is this position minus the one passed.
     */
    Position subtract(Position other);

}
