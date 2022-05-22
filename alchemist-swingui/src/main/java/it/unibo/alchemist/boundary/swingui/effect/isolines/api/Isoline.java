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
 *  An isoline (also contour line, isopleth, or isarithm) of a function of two variables is a
 *  curve along which the function has a constant value, so that the curve joins points of equal value.
 *
 *  Here the curve is approximated as a collection of 2D segments.
 */
public interface Isoline {

    /**
     * @return the value associated with this isoline
     */
    Number getValue();

    /**
     * @return the segments forming this isoline
     */
    Collection<Segment2D> getSegments();

}
