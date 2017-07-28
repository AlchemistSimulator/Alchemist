package it.unibo.alchemist.model.implementations.movestrategies.speed;

import static java.util.Objects.requireNonNull;

import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;

/**
 * This strategy dynamically tries to move the node adjusting its speed to
 * synchronize the reaction rate and the traces data.
 * 
 * @param <T>
 */
public abstract class TraceDependantSpeed<T> implements SpeedSelectionStrategy<T> {

    private static final long serialVersionUID = 8021140539083062866L;
    private final GPSTrace trace;
    private final Reaction<T> reaction;
    private final MapEnvironment<T> env;
    private final Node<T> node;

    /**
     * @param e
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     */
    public TraceDependantSpeed(final MapEnvironment<T> e, final Node<T> n, final Reaction<T> r) {
        env = requireNonNull(e);
        node = requireNonNull(n);
        reaction = requireNonNull(r);
        trace = requireNonNull(env.getTrace(node));
    }

    @Override
    public final double getCurrentSpeed(final Position target) {
        final double curTime = reaction.getTau().toDouble();
        final GPSPoint next = trace.getNextPosition(curTime);
        final double expArrival = next.getTime();
        if (curTime >= expArrival) {
            return Double.POSITIVE_INFINITY;
        }
        final double frequency = reaction.getRate();
        final double steps = (expArrival - curTime) * frequency;
        return computeDistance(env, node, target) / steps;
    }

    /**
     * @param environment
     *            the environment
     * @param curNode
     *            the node
     * @param targetPosition
     *            the target
     * @return an estimation of the distance between the node and the target
     *         position
     */
    protected abstract double computeDistance(MapEnvironment<T> environment, Node<T> curNode, Position targetPosition);

}
