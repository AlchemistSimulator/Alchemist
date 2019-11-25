/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines;

import it.unibo.alchemist.model.interfaces.Position2D;

import java.util.Collection;

/**
 * A factory
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
     * Create an IsolinesFinder object, which represents an Isoline finding algorithm.
     *
     * @param calculator - the algorithm you want to use
     *
     * @return the IsolineFinder
     */
    IsolinesFinder makeIsolinesFinder(IsolinesFinders calculator);

    /**
     * Enum containing all the available isolines finding algorithms.
     */
    enum IsolinesFinders {
        CONREC
    }
}
