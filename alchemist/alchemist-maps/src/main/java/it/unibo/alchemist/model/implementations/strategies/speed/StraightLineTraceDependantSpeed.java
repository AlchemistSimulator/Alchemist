/**
 * 
 */
package it.unibo.alchemist.model.implementations.strategies.speed;

import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * This {@link TraceDependantSpeed} uses the distance between coordinates for estimating the distance.
 * 
 * @param <T>
 */
public class StraightLineTraceDependantSpeed<T> extends TraceDependantSpeed<T> {

    private static final long serialVersionUID = 539968590628143027L;

    /**
     * @param e
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     */
    public StraightLineTraceDependantSpeed(final IMapEnvironment<T> e, final Node<T> n, final Reaction<T> r) {
        super(e, n, r);
    }

    @Override
    protected double computeDistance(final IMapEnvironment<T> environment, final Node<T> curNode, final Position targetPosition) {
        return environment.getPosition(curNode).getDistanceTo(targetPosition);
    }

}
