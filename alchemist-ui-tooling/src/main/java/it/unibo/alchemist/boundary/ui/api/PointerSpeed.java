/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.ui.api;

import java.awt.Point;

/**
 * Base type for any pointing device: it provides services to analyze the
 * pointer's movement.
 *
 */
public interface PointerSpeed {
    /**
     * Gets the pointer's current position.
     * 
     * @return a {@link Point} instance representing the pointer's current
     *         position
     */
    Point getCurrentPosition();

    /**
     * Gets the pointer's old position.
     * 
     * @return a {@link Point} instance representing the pointer's old
     *         position
     */
    Point getOldPosition();

    /**
     * Gets the vector [current position - old position].
     * 
     * @return a {@link Point} instance whose coordinates are [cP.x - oP.x;
     *         cP.y - cP.y]
     */
    Point getVariation();

    /**
     * Sets the pointer's current position and, consequently, updates the old
     * one.
     * 
     * @param point
     *            is the {@link Point} instance representing the pointer's
     *            current position
     */
    void setCurrentPosition(Point point);
}
