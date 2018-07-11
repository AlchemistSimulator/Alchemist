/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.environments;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.util.FlexibleQuadTree;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;

/**
 * 
 * Models a bidimensional environment.
 * 
 * @param <T> concentration type
 * @param <P> {@link Position2D} type
 */
public abstract class Abstract2DEnvironment<T, P extends Position2D<P>> extends AbstractEnvironment<T, P> {

    private static final long serialVersionUID = 1L;

    private double minX = POSITIVE_INFINITY, maxX = NEGATIVE_INFINITY, minY = POSITIVE_INFINITY,
            maxY = NEGATIVE_INFINITY;

    /**
     * 
     */
    protected Abstract2DEnvironment() {
        super(new FlexibleQuadTree<>());
    }

    /**
     * Subclasses can override this method if they will to modify the actual
     * position a node gets inserted in (e.g. to restrict the areas in which a node
     * can be)
     */
    @Override
    protected P computeActualInsertionPosition(final Node<T> node, final P p) {
        return p;
    }

    @Override
    public final int getDimensions() {
        return 2;
    }

    @Override
    public final double[] getOffset() {
        return new double[] { minX <= maxX ? minX : NaN, minY <= maxY ? minY : NaN };
    }

    @Override
    public final double[] getSize() {
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
    protected final void includeObject(final P pos) {
        final double x = pos.getCoordinate(0);
        final double y = pos.getCoordinate(1);
        includeObject(x, x, y, y);
    }

    /**
     * Subclasses may override this method if they want to change the way a node
     * moves towards some direction. The current implementation internally calls
     * {@link #moveNodeToPosition(Node, Position2D)}, as such, overriding that
     * method may suffice.
     */
    @Override
    public void moveNode(final Node<T> node, final P direction) {
        final P oldcoord = getPosition(node);
        moveNodeToPosition(node, oldcoord.add(direction));
    }

    /**
     * Subclasses may override this method if they want to change the way a node
     * moves towards some aboslute position. Overriding this method will also
     * influence {@link #moveNode(Node, Position2D)}, as it calls this method in the
     * current implementation
     */
    @Override
    public void moveNodeToPosition(final Node<T> node, final P newpos) {
        includeObject(newpos);
        setPosition(node, newpos);
        updateNeighborhood(node);
        final Simulation<T, P> sim = getSimulation();
        if (sim != null) {
            sim.nodeMoved(node);
        }
    }

    /**
     * Subclasses may want to override this method to hook to the node addition
     * event. Overriders should call the super implementation, as it ensures the
     * environment bounds are updated considering the newly included object.
     */
    @Override
    protected void nodeAdded(final Node<T> node, final P position, final Neighborhood<T> neighborhood) {
        /*
         * Size update
         */
        includeObject(position);
    }

}
