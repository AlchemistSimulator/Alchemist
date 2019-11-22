/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/*
 * Render.java
 *
 * Created on 5 August 2001, 18:57
 */

package it.unibo.alchemist.boundary.gui.isolines.conrec;

/**
 * A class implements the Render interface and drawContour method
 * to draw contours.
 *
 * author  Bradley White
 * @version 1.0
 */
public interface Render {

    /**
     * drawContour - interface for implementing the user supplied method to
     * render the contours.
     *
     * Draws a line between the start and end coordinates.
     *
     * @param startX    - start coordinate for X
     * @param startY    - start coordinate for Y
     * @param endX      - end coordinate for X
     * @param endY      - end coordinate for Y
     * @param contourLevel - Contour level for line.
     */
    void drawContour(double startX, double startY, double endX, double endY, double contourLevel);

}


