package it.unibo.alchemist.model.implementations.environments;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.util.FlexibleQuadTree;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

public abstract class Abstract2DEnvironment<T, P extends Position<? extends P>> extends AbstractEnvironment<T, P> {

    private static final long serialVersionUID = 1L;

    private double minX = POSITIVE_INFINITY, maxX = NEGATIVE_INFINITY, minY = POSITIVE_INFINITY,
            maxY = NEGATIVE_INFINITY;

    protected Abstract2DEnvironment() {
        super(new FlexibleQuadTree<>());
    }

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

    @Override
    public void moveNode(final Node<T> node, final P direction) {
        final P oldcoord = getPosition(node);
        moveNodeToPosition(node, sumVectors(oldcoord, direction));
    }

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

    @Override
    protected void nodeAdded(final Node<T> node, final P position, final Neighborhood<T> neighborhood) {
        /*
         * Size update
         */
        includeObject(position);
    }

    public abstract P sumVectors(P p1, P p2);


}
