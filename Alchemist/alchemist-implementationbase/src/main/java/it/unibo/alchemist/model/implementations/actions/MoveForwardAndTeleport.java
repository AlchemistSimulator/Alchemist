/**
 * 
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * @param <T>
 *            Concentration type
 */
public class MoveForwardAndTeleport<T> extends AbstractMoveNode<T> {

    private static final long serialVersionUID = 6853946136578807021L;
    private final double dx, minx, maxx;
    private double y = Double.NaN;

    /**
     * @param environment the environment
     * @param node the node
     * @param deltaX how far along the x axis the node should move each time the action is triggered
     * @param minX minimum x point
     * @param maxX maximum x point
     */
    public MoveForwardAndTeleport(final Environment<T> environment, final Node<T> node, final double deltaX, final double minX, final double maxX) {
        super(environment, node, true);
        dx = deltaX;
        minx = minX;
        maxx = maxX;
    }

    @Override
    public MoveForwardAndTeleport<T> cloneOnNewNode(final Node<T> n, final Reaction<T> r) {
        return new MoveForwardAndTeleport<>(getEnvironment(), n, dx, minx, maxx);
    }

    @Override
    public Position getNextPosition() {
        final Position cur = getEnvironment().getPosition(getNode());
        if (Double.isNaN(y)) {
            y = cur.getCoordinate(1);
        }
        final double x = cur.getCoordinate(0);
        if (x > maxx) {
            return new Continuous2DEuclidean(minx, y);
        }
        return new Continuous2DEuclidean(x + dx, y);
    }

}
