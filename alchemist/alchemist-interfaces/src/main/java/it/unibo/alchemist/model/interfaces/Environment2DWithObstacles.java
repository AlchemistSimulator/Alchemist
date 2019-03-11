/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.util.List;

/**
 * @param <W>
 * @param <T>
 * @param <P>
 */
public interface Environment2DWithObstacles<W extends Obstacle2D, T, P extends Position<? extends P>> extends Environment<T, P> {

    /**
     * Adds an {@link Obstacle2D} to this environment.
     * 
     * @param o
     *            the {@link Obstacle2D} to add
     */
    void addObstacle(W o);

    /**
     * @return a list of all the Obstacles in this
     *         {@link Environment2DWithObstacles}
     */
    List<W> getObstacles();

    /**
     * Given a point and a range, retrieves all the obstacles within.
     * 
     * @param centerx
     *            the x coordinate of the center
     * @param centery
     *            the y coordinate of the center
     * @param range
     *            the range to scan
     * @return the list of Obstacles
     */
    List<W> getObstaclesInRange(double centerx, double centery, double range);

    /**
     * @return true if this environment has mobile obstacles obstacles, false if
     *         the obstacles are static
     */
    boolean hasMobileObstacles();

    /**
     * Checks whether there is at least an obstacle intersecting the line connecting [sx, sy] and [ex, ey].
     * 
     * @param sx start x
     * @param sy start y
     * @param ex end x
     * @param ey end y
     * @return true if the line connecting [sx, sy] and [ex, ey] touches an obstacle
     */
    boolean intersectsObstacle(double sx, double sy, double ex, double ey);

    /**
     * Checks whether there is at least an obstacle intersecting the line connecting p1 and p2.
     * @param p1 start position
     * @param p2 end position
     * @return true if the line connecting p1 and p2 touches an obstacle
     */
    boolean intersectsObstacle(P p1, P p2);

    /**
     * This method must calculate the RELATIVE next allowed position given the
     * current position and the position in which the node wants to move. For
     * example, if your node is in position [2,3], wants to move to [3,4] but
     * the next allowed position (because, e.g., of physical obstacles) is
     * [2.5,3.5], the result must be a {@link Position} containing coordinates
     * [0.5,0.5], so it's the relative movement considering the starting
     * position as origin.
     * 
     * @param ox
     *            The current X position
     * @param oy
     *            The current Y position
     * @param nx
     *            The requested X position
     * @param ny
     *            The requested Y position
     * 
     * @return the next allowed position, where the node can actually move. This
     *         position MUST be considered as a vector whose start point is in
     *         [ox, oy].
     */
    P next(double ox, double oy, double nx, double ny);


    /**
     * Removes an {@link Obstacle2D} from this environment.
     * 
     * @param o
     *            the {@link Obstacle2D} to add
     * @return true if the {@link Obstacle2D} has actually been removed
     */
    boolean removeObstacle(W o);

}
