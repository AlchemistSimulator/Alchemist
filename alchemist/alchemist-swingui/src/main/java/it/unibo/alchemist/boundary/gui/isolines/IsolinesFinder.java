/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines;

import java.util.Collection;

/**
 * Defines an object capable of finding isolines (i.e. an isolines finding algorithm).
 */
public interface IsolinesFinder {

    /**
     * Defines a basic bidimensional function.
     */
    @FunctionalInterface
    interface BidimensionalFunction {

        Number apply(Number x, Number y);

    }

    /**
     * Find the isolines of the given function. You can specify which isolines will be extracted with the
     * levels parameter: for each value included in the collection, the corresponding isoline will be
     * extracted. Isolines will be calculated within a rectangular region defined by two opposite vertexes.
     * This means that the algorithm will not consider the space outside the given region at all.
     *
     * @param f      - the function for which to calculate the isolines
     * @param x1     - x coordinate of vertex 1, defining the rectangular space within which isolines will be calculated
     * @param y1     - y coordinate of vertex 1, defining the rectangular space within which isolines will be calculated
     * @param x2     - x coordinate of vertex 2, defining the rectangular space within which isolines will be calculated
     * @param y2     - y coordinate of vertex 2, defining the rectangular space within which isolines will be calculated
     * @param levels - collection containing the levels of the isolines that will be calculated
     *
     * @return the isolines
     */
    Collection<Isoline> findIsolines(BidimensionalFunction f, Number x1, Number y1, Number x2, Number y2, Collection<Number> levels);

    /**
     * Find the isolines of the given function. This method is equivalent to {@link IsolinesFinder#findIsolines(BidimensionalFunction, Number, Number, Number, Number, Collection)},
     * with the difference that it allows you to specify the diagonal of the rectangular region, instead of the four
     * vertexes separately.
     * @param f        - the function for which to calculate the isolines
     * @param diagonal - the diagonal of the rectangular space within which isolines will be calculated
     * @param levels   - collection containing the levels of the isolines that will be calculated
     *
     * @return the isolines
     */
    Collection<Isoline> findIsolines(BidimensionalFunction f, Segment2D diagonal, Collection<Number> levels);

}
