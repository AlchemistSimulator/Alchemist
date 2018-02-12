package it.unibo.alchemist.model.implementations.actions;

import java.util.Objects;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;

/**
 * An abstract class that factorizes code for multiple different movements. With
 * three strategies can be defined: the next target to be reached, the routing
 * strategy to adopt, the speed to move at.
 *
 * @param <T>
 */
public abstract class AbstractConfigurableMoveNode<T> extends AbstractMoveNode<T> {

    private static final long serialVersionUID = 1L;
    private final TargetSelectionStrategy target;
    private final SpeedSelectionStrategy speed;
    private final RoutingStrategy routing;
    private Route<?> route;
    private Position end;
    private int curStep;

    /**
     * Builds a new move node action. By default the movements are relative.
     * 
     * @param environment
     *            The environment where to move
     * @param node
     *            The node to which this action belongs
     * @param routing
     *            the routing strategy
     * @param target
     *            the strategy used to compute the next target
     * @param speed
     *            the speed selection strategy
     */
    protected AbstractConfigurableMoveNode(final Environment<T> environment,
            final Node<T> node,
            final RoutingStrategy routing,
            final TargetSelectionStrategy target,
            final SpeedSelectionStrategy speed) {
        this(environment, node, routing, target, speed, false);
    }

    /**
     * @param environment
     *            The environment where to move
     * @param node
     *            The node to which this action belongs
     * @param routing
     *            the routing strategy
     * @param target
     *            the strategy used to compute the next target
     * @param speed
     *            the speed selection strategy
     * @param isAbsolute
     *            if set to true, the environment expects the movement to be
     *            expressed in absolute coordinates. It means that, if a node in
     *            (1,1) wants to move to (2,3), its getNextPosition() must
     *            return (2,3). If false, a relative coordinate is expected, and
     *            the method for the same effect must return (1,2).
     */
    protected AbstractConfigurableMoveNode(final Environment<T> environment,
            final Node<T> node,
            final RoutingStrategy routing,
            final TargetSelectionStrategy target,
            final SpeedSelectionStrategy speed,
            final boolean isAbsolute) {
        super(environment, node, isAbsolute);
        this.speed = Objects.requireNonNull(speed);
        this.target = Objects.requireNonNull(target);
        this.routing = Objects.requireNonNull(routing);
    }

    @Override
    public final Position getNextPosition() {
        final Position previousEnd = end;
        end = target.getTarget();
        if (!end.equals(previousEnd)) {
            resetRoute();
        }
        double maxWalk = speed.getCurrentSpeed(end);
        final Environment<T> env = getEnvironment();
        final Node<T> node = getNode();
        Position curPos = env.getPosition(node);
        if (curPos.getDistanceTo(end) <= maxWalk) {
            final Position destination = end;
            end = target.getTarget();
            resetRoute();
            return isAbsolute() ? destination : destination.subtract(curPos);
        }
        if (route == null) {
            route = routing.computeRoute(curPos, end);
        }
        if (route.size() < 1) {
            resetRoute();
            return getDestination(curPos, end, maxWalk);
        }
        Position target = null;
        double toWalk;
        do {
            target = route.getPoint(curStep);
            toWalk = target.getDistanceTo(curPos);
            if (toWalk > maxWalk) {
                return getDestination(curPos, target, maxWalk);
            }
            curStep++;
            maxWalk -= toWalk;
            curPos = target;
        } while (curStep != route.size());
        /*
         * I've followed the whole route
         */
        resetRoute();
        target = end;
        return getDestination(curPos, target, maxWalk);
    }

    /**
     * @param current the current position of the node
     * @param target the target that should be reached
     * @param maxWalk how far the node can move
     * @return the position that the node reaches
     */
    protected abstract Position getDestination(Position current, Position target, double maxWalk);

    /**
     * @return the current target
     */
    protected final Position getTargetPoint() {
        return end;
    }

    /**
     * Resets the current route, e.g. because the target has been reached
     */
    protected final void resetRoute() {
        route = null;
        curStep = 0;
    }

    /**
     * @param p
     *            the new target
     */
    protected final void setTargetPoint(final Position p) {
        end = p;
    }

    /**
     * @return the current route, or null if no route is currently being followed
     */
    protected final Route<?> getCurrentRoute() {
        return route;
    }
}
