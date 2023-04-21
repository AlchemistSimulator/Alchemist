/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Node;

/**
 * This class represents a 2D continuous environment with spatial limitations.
 * Those limitations will prevent nodes to move in positions which are not
 * allowed.
 * 
 * @param <T> concentration type
 */
public abstract class LimitedContinuos2D<T> extends Continuous2DEnvironment<T> {

    private static final long serialVersionUID = -7838255122589911058L;


    /**
     * @param incarnation the current incarnation.
     */
    public LimitedContinuos2D(final Incarnation<T, Euclidean2DPosition> incarnation) {
        super(incarnation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveNodeToPosition(final Node<T> node, final Euclidean2DPosition newPos) {
        final double[] cur = getPosition(node).getCoordinates();
        final double[] np = newPos.getCoordinates();
        // Calculate the next position allowed
        final Euclidean2DPosition next = next(cur[0], cur[1], np[0], np[1]);
        super.moveNodeToPosition(node, next);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean nodeShouldBeAdded(final Node<T> node, final Euclidean2DPosition p) {
        /*
         * Takes into account both obstacles and other nodes.
         */
        return isAllowed(p) && super.nodeShouldBeAdded(node, p);
    }

    /**
     * This method must calculate the ABSOLUTE next allowed position given the
     * current position and the position in which the node wants to move. For
     * example, if your node is in position [2,3], wants to move to [3,4] but
     * the next allowed position (because, e.g., of physical obstacles) is
     * [2.5,3.5], the result must be a Position containing coordinates
     * [2.5,3.5].
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
    protected abstract Euclidean2DPosition next(double ox, double oy, double nx, double ny);

    /**
     * Checks whether a position is allowed to be occupied by a node in this
     * environment.
     * 
     * @param p
     *            the position to check
     * @return true if the position is allowed
     */
    protected abstract boolean isAllowed(Euclidean2DPosition p);

}
