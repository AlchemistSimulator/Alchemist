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
import java.awt.geom.Dimension2D;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * A Wormhole (in this context) is an entity that "connects" two worlds: the
 * "environment" and the "view". Above all it provides services to convert
 * coordinates from the "environment-space" to the "view-space".
 * <code>IWormhole2D</code> is the type of a wormhole whose both environment and
 * view are bi-dimensional spaces. <br>
 * <br>
 * <strong>Terminology:</strong> <br>
 * <br>
 * - "Environment" is the 'rectangle' we need to render ON the view.<br>
 * - "View" is a 'window' that let us see the environment.<br>
 * - "Environment-space" is the algebraic space on which 'lies' the environment.<br>
 * - "View-space" is the algebraic space on which 'lies' the view.<br>
 * - "Env" before a point's name => it refers to a point into the
 * environment-space.<br>
 * - "View" before a point's name => it refers to a point into the view-space.<br>
 * - "Position" is the point of the view-space every transformation applied to
 * the environment refers to: e.g. if I want to move the environment, I have to
 * change the position; it is also the point the environment rotates around.<br>
 * - "EnvOffset" is the vector from (0; 0) into env-space to the left-bottom
 * corner of the part of the environment we want to render. E.g. if I am
 * representing a map with Earth-coordinates (16; 48), the intersection between
 * the prime meridian and the equator is (0; 0), so I have to set the envOffset
 * to (16; 48) in order to see the "beginning" of the map on the left-bottom
 * corner of the view.<br>
 * 

 */
public interface IWormhole2D {

    /**
     * Wormhole mode.
     * 
     */
    enum Mode {
        /**
         * No stretch allowed
         */
        ISOMETRIC,
        /**
         * Stretch to adapt to view
         */
        ADAPT_TO_VIEW,
        /**
         * Stretch is defined by user
         */
        SETTABLE,
        /**
         * Uses spherical coordinates, no stretch allowed
         */
        MAP;
    };

    /**
     * Converts a point from the view-space to the env-space.
     * 
     * @param viewPoint
     *            is the {@link Point} object whose coordinates are from
     *            view-space
     * @return a {@link Position} object whose coordinates are from env-space
     */
    Position getEnvPoint(Point viewPoint);

    /**
     * Gets the rendering mode.
     * 
     * @return a {@link IWormhole2D.Mode} value
     */
    Mode getMode();

    /**
     * Converts a point from the env-space to the view-space.
     * 
     * @param envPoint
     *            is the {@link Position} object whose coordinates are from
     *            env-space
     * @return a {@link Point} object whose coordinates are from view-space
     */
    Point getViewPoint(Position envPoint);

    /**
     * Gets the Position.
     * 
     * @return a {@link Point} object representing the Position
     */
    Point getViewPosition();

    /**
     * Gets the view's size.
     * 
     * @return a {@link Dimension2D} object containing the view's width and
     *         height
     */
    Dimension2D getViewSize();

    /**
     * Gets the zoom factor.
     * 
     * @return a <code>double</code> representing the zoom factor
     */
    double getZoom();

    /**
     * Check if a point of the view-space is "visible", i.e. it is inside the
     * view.
     * 
     * @param viewPoint
     *            is the {@link Point} to check
     * @return <code>true</code> if it is visible, <code>false</code> instead
     */
    boolean isInsideView(Point viewPoint);

    /**
     * Rotates around a point into the view-space.
     * 
     * @param p
     *            is the {@link Point}
     * @param a
     *            is the absolute angle (in radians)
     */
    void rotateAroundPoint(Point p, double a);

    /**
     * Sets the position to the view-point corresponding to
     * <code>envPoint</code>.
     * 
     * @param envPoint
     *            is the {@link Position} object representing the new position
     *            with env-coordinates
     */
    void setEnvPosition(Position envPoint);

    /**
     * Automatically sets the zoom rate in order to make the environment
     * entirely visible on the view.
     */
    void optimalZoom();

    /**
     * Rotates the environment around the Position.
     * 
     * @param rad
     *            is the <code>double</code> value representing the angle
     *            expressed with radians
     */
    void setRotation(double rad);

    /**
     * Sets the Position to <code>viewPoint</code>.
     * 
     * @param viewPoint
     *            is the {@link Point} object representing the new position
     *            with view-coordinates
     */
    void setViewPosition(Point viewPoint);

    /**
     * Changes the zoom factor.
     * 
     * @param value
     *            is the <code>double</code> value representing the new zoom
     */
    void setZoom(double value);

    /**
     * Zooms on a point into the view-space.
     * 
     * @param p
     *            is the {@link Point}
     * @param z
     *            is the absolute zoom rate
     */
    void zoomOnPoint(Point p, double z);

    /**
     * Points the center of the view on the center of the environment.
     */
    void center();
}
