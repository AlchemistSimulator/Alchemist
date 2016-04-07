/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.interfaces;

/**
 * A class that implements the <code>IZoomManager</code> interface is able to
 * convert the sliding of any physical/virtual device/control into a positive
 * <code>double</code> value that represents a zoom rate.
 * 

 */
public interface ZoomManager extends ISlideInputManager {
    /**
     * Gets the zoom rate.
     * 
     * @return a <code>double</code> value representing the zoom rate
     */
    double getZoom();

    /**
     * Sets the zoom rate.
     * 
     * @param rate
     *            is the <code>double</code> value representing the zoom rate
     */
    void setZoom(double rate);
}
