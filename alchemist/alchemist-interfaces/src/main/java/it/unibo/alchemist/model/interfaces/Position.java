/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An interface to represent a generic coordinates system.
 * 
 * @param <P>
 *            the actual {@link Position} type: this strategy allows to
 *            progressively refine the {@link Position} by inheritance, allowing
 *            for specifying incrementally fine grained model elements.
 */
public interface Position<P extends Position<?>> extends Serializable {

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
    List<? extends P> boundingBox(double range);

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
    double getDistanceTo(P p);

    /**
     * Considers both positions as vectors, and sums them.
     * 
     * @param other the other position
     * @return a new {@link Position} that is the sum of the two.
     */
    P add(P other);

    /**
     * Considers both positions as vectors, and returns the difference between this position and the passed one.
     * 
     * @param other the other position
     * @return a new {@link Position} that is this position minus the one passed.
     */
    P subtract(P other);

    /**
     * Tries to compute distance between arbitrary positions, looking for a common
     * supertype.
     * 
     * @param p1
     *            first position
     * @param p2
     *            second position
     * @param <P>
     *            position type
     * @return the distance between the positions, if computable
     */
    @SuppressWarnings("unchecked")
    static <P extends Position<? extends P>> double distanceTo(P p1, P p2) {
        final Class<?> p1Class = Objects.requireNonNull(p1).getClass();
        final Class<?> p2Class = Objects.requireNonNull(p2).getClass();
        @SuppressWarnings("rawtypes")
        final Position p1Unsafe = (Position) p1;
        @SuppressWarnings("rawtypes")
        final Position p2Unsafe = (Position) p2;
        if (p1Class == p2Class || p1Class.isAssignableFrom(p2Class)) {
            return p1Unsafe.getDistanceTo(p2Unsafe);
        }
        if (p2Class.isAssignableFrom(p1Class)) {
            return p2Unsafe.getDistanceTo(p1Unsafe);
        }
        // Check arguments of distanceTo
        Method p1distTo = Arrays.stream(p1Class.getDeclaredMethods())
            .filter(Method::isAccessible)
            .filter(it -> it.getReturnType() == double.class)
            .filter(it -> it.getParameterCount() == 1)
            .filter(it -> it.getName().equals("getDistanceTo"))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException(p1 + " has no valid getDistanceTo() method"));
        if (p1distTo.getParameterTypes()[0].isAssignableFrom(p2Class)) {
            return p1Unsafe.getDistanceTo(p2Unsafe);
        }
        Method p2distTo = Arrays.stream(p2Class.getDeclaredMethods())
                .filter(Method::isAccessible)
                .filter(it -> it.getReturnType() == double.class)
                .filter(it -> it.getParameterCount() == 1)
                .filter(it -> it.getName().equals("getDistanceTo"))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(p1 + " has no valid getDistanceTo() method"));
        if (p2distTo.getParameterTypes()[0].isAssignableFrom(p1Class)) {
            return p2Unsafe.getDistanceTo(p1Unsafe);
        }
        throw new IllegalArgumentException("computing distance between " + p1 + " and " + p2 + " is impossible");
    }

}
