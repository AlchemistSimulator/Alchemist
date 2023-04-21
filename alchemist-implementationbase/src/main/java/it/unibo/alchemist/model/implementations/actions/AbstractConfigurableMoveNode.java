/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;

import java.util.Objects;

/**
 * An abstract class that factorizes code for multiple different movements. With
 * three strategies can be defined: the next target to be reached, the routing
 * strategy to adopt, the speed to move at.
 *
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public abstract class AbstractConfigurableMoveNode<T, P extends Position<P>> extends AbstractMoveNode<T, P> {

    private static final long serialVersionUID = 1L;
    private final TargetSelectionStrategy<T, P> targetSelectionStrategy;
    private final SpeedSelectionStrategy<T, P> speedSelectionStrategy;
    private final RoutingStrategy<T, P> routingStrategy;
    private Route<P> route;
    private P end;
    private int curStep;

    /**
     * Builds a new move node action. By default the movements are relative.
     * 
     * @param environment
     *            The environment where to move
     * @param node
     *            The node to which this action belongs
     * @param routingStrategy
     *            the routing strategy
     * @param target
     *            the strategy used to compute the next target
     * @param speedSelectionStrategy
     *            the speed selection strategy
     */
    protected AbstractConfigurableMoveNode(final Environment<T, P> environment,
        final Node<T> node,
        final RoutingStrategy<T, P> routingStrategy,
        final TargetSelectionStrategy<T, P> target,
        final SpeedSelectionStrategy<T, P> speedSelectionStrategy
    ) {
        this(environment, node, routingStrategy, target, speedSelectionStrategy, false);
    }

    /**
     * @param environment
     *            The environment where to move
     * @param node
     *            The node to which this action belongs
     * @param routingStrategy
     *            the routing strategy
     * @param target
     *            the strategy used to compute the next target
     * @param speedSelectionStrategy
     *            the speed selection strategy
     * @param isAbsolute
     *            if set to true, the environment expects the movement to be
     *            expressed in absolute coordinates. It means that, if a node in
     *            (1,1) wants to move to (2,3), its getNextPosition() must
     *            return (2,3). If false, a relative coordinate is expected, and
     *            the method for the same effect must return (1,2).
     */
    protected AbstractConfigurableMoveNode(final Environment<T, P> environment,
        final Node<T> node,
        final RoutingStrategy<T, P> routingStrategy,
        final TargetSelectionStrategy<T, P> target,
        final SpeedSelectionStrategy<T, P> speedSelectionStrategy,
        final boolean isAbsolute
    ) {
        super(environment, node, isAbsolute);
        this.speedSelectionStrategy = Objects.requireNonNull(speedSelectionStrategy);
        this.targetSelectionStrategy = Objects.requireNonNull(target);
        this.routingStrategy = Objects.requireNonNull(routingStrategy);
    }

    @Override
    public final P getNextPosition() {
        final P previousEnd = end;
        end = targetSelectionStrategy.getTarget();
        if (!end.equals(previousEnd)) {
            resetRoute();
        }
        double maxWalk = speedSelectionStrategy.getNodeMovementLength(end);
        final Environment<T, P> environment = getEnvironment();
        final Node<T> node = getNode();
        P curPos = environment.getPosition(node);
        if (curPos.distanceTo(end) <= maxWalk) {
            final P destination = end;
            end = targetSelectionStrategy.getTarget();
            resetRoute();
            return isAbsolute() ? destination : destination.minus(curPos.getCoordinates());
        }
        if (route == null) {
            route = routingStrategy.computeRoute(curPos, end);
        }
        if (route.size() < 1) {
            resetRoute();
            return interpolatePositions(curPos, end, maxWalk);
        }
        do {
            final P target = route.getPoint(curStep);
            final double toWalk = target.distanceTo(curPos);
            if (toWalk > maxWalk) {
                /*
                 * I can arrive at most at maxWalk
                 */
                return interpolatePositions(curPos, target, maxWalk);
            }
            curStep++;
            maxWalk -= toWalk;
            curPos = target;
        } while (curStep != route.size());
        /*
         * I've followed the whole route
         */
        resetRoute();
        return interpolatePositions(curPos, end, maxWalk);
    }

    /**
     * Given a start position (current), a desired target position (target), and a maximum walkable distance (maxWalk),
     * this method computes the actual position reached by the moving node, in absolute or relative coordinates
     * depending on the value of isAbsolute in the constructor.
     *
     * @param current the current position of the node
     * @param target the target that should be reached
     * @param maxWalk how far the node can move
     * @return the position that the node reaches
     */
    protected abstract P interpolatePositions(P current, P target, double maxWalk);

    /**
     * @return the current target
     */
    protected final P getTargetPoint() {
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
    protected final void setTargetPoint(final P p) {
        end = p;
    }

    /**
     * @return the current route, or null if no route is currently being followed
     */
    protected final Route<?> getCurrentRoute() {
        return route;
    }

    /**
     * @return the {@link RoutingStrategy}
     */
    protected final RoutingStrategy<T, P> getRoutingStrategy() {
        return routingStrategy;
    }

    /**
     * @return the {@link SpeedSelectionStrategy}
     */
    protected final SpeedSelectionStrategy<T, P> getSpeedSelectionStrategy() {
        return speedSelectionStrategy;
    }

    /**
     * @return the {@link TargetSelectionStrategy}
     */
    protected final TargetSelectionStrategy<T, P> getTargetSelectionStrategy() {
        return targetSelectionStrategy;
    }
}
