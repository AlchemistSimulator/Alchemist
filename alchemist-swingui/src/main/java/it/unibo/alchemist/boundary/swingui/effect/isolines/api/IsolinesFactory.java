/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.isolines.api;

import java.util.Collection;

/**
 * A factory for the creation of the basic astractions contained in this package.
 */
public interface IsolinesFactory {

    /**
     * Create a 2D segment.
     *
     * @param x1 - x coordinate of point 1
     * @param y1 - y coordinate of point 1
     * @param x2 - x coordinate of point 2
     * @param y2 - y coordinate of point 2
     *
     * @return the segment
     */
    Segment2D makeSegment(Number x1, Number y1, Number x2, Number y2);

    /**
     * Create an Isoline.
     *
     * @param value    - the value associated to the isoline to be created
     * @param segments - the segments forming the isoline
     *
     * @return the isoline
     */
    Isoline makeIsoline(Number value, Collection<Segment2D> segments);

    /**
     * Create an IsolinesFinder object, capable of finding isolines.
     * As different finders could be available - each one extracting
     * isolines in a different way - you can specify which one to use with
     * the algorithm parameter.
     *
     * @param algorithm - the algorithm you want to use
     *
     * @return the IsolineFinder
     */
    IsolinesFinder makeIsolinesFinder(IsolineFinders algorithm);

    /**
     * Enum containing all the available isolines finding algorithms.
     */
    enum IsolineFinders {
        /**
         * For each class extending IsolinesFinder there should be an element in this enum.
         */
        CONREC
    }
}
