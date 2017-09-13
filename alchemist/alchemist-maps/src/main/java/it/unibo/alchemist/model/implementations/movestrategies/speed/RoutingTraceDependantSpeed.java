/**
 * 
 */
package it.unibo.alchemist.model.implementations.movestrategies.speed;

import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Vehicle;

/**
 * This {@link TraceDependantSpeed} strategy computes the remaining distance by
 * relying on maps data for a selected {@link Vehicle}.
 * 
 * @param <T>
 */
public class RoutingTraceDependantSpeed<T> extends TraceDependantSpeed<T> {

    private static final long serialVersionUID = -2195494825891818353L;
    private final Vehicle v;

    /**
     * @param e
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     * @param vehicle
     *            the vehicle
     */
    public RoutingTraceDependantSpeed(final MapEnvironment<T> e, final Node<T> n, final Reaction<T> r, final Vehicle vehicle) {
        super(e, n, r);
        v = vehicle;
    }

    @Override
    protected double computeDistance(final MapEnvironment<T> environment, final Node<T> curNode, final Position targetPosition) {
        return environment.computeRoute(curNode, targetPosition, v).length();
    }

}
