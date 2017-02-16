/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.util.FlexibleQuadTree;
import org.danilopianini.util.SpatialIndex;

import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * @param <T>
 */
public class Continuous2DEnvironment<T> extends AbstractLinkingRuleEnvironment<T> {


    private static final long serialVersionUID = 1056357696289385352L;
    private double minX = POSITIVE_INFINITY,
                   maxX = NEGATIVE_INFINITY,
                   minY = POSITIVE_INFINITY,
                   maxY = NEGATIVE_INFINITY;

    /**
     * Builds a new Continuous2DEnvironment, using a {@link FlexibleQuadTree} as {@link SpatialIndex}.
     */
    public Continuous2DEnvironment() {
        super(new FlexibleQuadTree<>());
    }

    @Override
    protected Position computeActualInsertionPosition(final Node<T> node, final Position p) {
        return p;
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public double[] getOffset() {
        return new double[] {
                minX <= maxX ? minX : NaN,
                minY <= maxY ? minY : NaN
        };
    }

    @Override
    public double[] getSize() {
        return new double[] { Math.max(0, maxX - minX), Math.max(0, maxY - minY) };
    }

    /**
     * Allows to extend the size of the environment by adding some object.
     * 
     * @param startx
     *            minimum x position of the object
     * @param endx
     *            maximum x position of the object
     * @param starty
     *            minimum y position of the object
     * @param endy
     *            maximum y position of the object
     */
    protected final void includeObject(final double startx, final double endx, final double starty, final double endy) {
        if (startx < minX) {
            minX = FastMath.nextDown(startx);
        }
        if (starty < minY) {
            minY = FastMath.nextDown(starty);
        }
        if (endx > maxX) {
            maxX = FastMath.nextUp(endx);
        }
        if (endy > maxY) {
            maxY = FastMath.nextUp(endy);
        }
        assert minX < maxX;
        assert minY < maxY;
    }

    /**
     * Updates the environment size to include the provided position.
     * 
     * @param pos
     *            the position to include
     */
    protected final void includeObject(final Position pos) {
        assert pos.getDimensions() == 2;
        final double x = pos.getCoordinate(0);
        final double y = pos.getCoordinate(1);
        includeObject(x, x, y, y);
    }


    @Override
    public void moveNode(final Node<T> node, final Position direction) {
        final Position oldcoord = getPosition(node);
        moveNodeToPosition(node, oldcoord.add(direction));
    }

    @Override
    public void moveNodeToPosition(final Node<T> node, final Position newpos) {
        includeObject(newpos);
        setPosition(node, newpos);
        updateNeighborhood(node);
        getSimulation().nodeMoved(node);
    }

    @Override
    protected void nodeAdded(final Node<T> node, final Position position, final Neighborhood<T> neighborhood) {
        /*
         * Size update
         */
        includeObject(position);
    }

    @Override
    protected void nodeRemoved(final Node<T> node, final Neighborhood<T> neighborhood) {
        /*
         * No action required.
         */
    }

    @Override
    protected boolean nodeShouldBeAdded(final Node<T> node, final Position p) {
        return true;
    }

}
