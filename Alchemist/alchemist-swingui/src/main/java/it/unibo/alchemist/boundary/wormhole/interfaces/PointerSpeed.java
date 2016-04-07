/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.interfaces;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Base type for any pointing device: it provides services to analyze the
 * pointer's movement.
 * 

 */
public interface PointerSpeed {
    /**
     * Gets the pointer's current position.
     * 
     * @return a {@link Point2D} instance representing the pointer's current
     *         position
     */
    Point getCurrentPosition();

    /**
     * Gets the pointer's old position.
     * 
     * @return a {@link Point2D} instance representing the pointer's old
     *         position
     */
    Point getOldPosition();

    /**
     * Gets the vector [current position - old position].
     * 
     * @return a {@link Point2D} instance whose coordinates are [cP.x - oP.x;
     *         cP.y - cP.y]
     */
    Point getVariation();

    /**
     * Sets the pointer's current position and, consequently, updates the old
     * one.
     * 
     * @param point
     *            is the {@link Point2D} instance representing the pointer's
     *            current position
     */
    void setCurrentPosition(Point point);
}
