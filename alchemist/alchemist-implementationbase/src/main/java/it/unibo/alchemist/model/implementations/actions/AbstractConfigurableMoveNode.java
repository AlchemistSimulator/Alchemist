/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
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
 * @param <P>
 */
public abstract class AbstractConfigurableMoveNode<T, P extends Position<P>> extends AbstractMoveNode<T, P> {

    private static final long serialVersionUID = 1L;
    private final TargetSelectionStrategy<P> target;
    private final SpeedSelectionStrategy<P> speed;
    private final RoutingStrategy<P> routing;
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
     * @param routing
     *            the routing strategy
     * @param target
     *            the strategy used to compute the next target
     * @param speed
     *            the speed selection strategy
     */
    protected AbstractConfigurableMoveNode(final Environment<T, P> environment,
            final Node<T> node,
            final RoutingStrategy<P> routing,
            final TargetSelectionStrategy<P> target,
            final SpeedSelectionStrategy<P> speed) {
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
    protected AbstractConfigurableMoveNode(final Environment<T, P> environment,
            final Node<T> node,
            final RoutingStrategy<P> routing,
            final TargetSelectionStrategy<P> target,
            final SpeedSelectionStrategy<P> speed,
            final boolean isAbsolute) {
        super(environment, node, isAbsolute);
        this.speed = Objects.requireNonNull(speed);
        this.target = Objects.requireNonNull(target);
        this.routing = Objects.requireNonNull(routing);
    }

    @Override
    public final P getNextPosition() {
        final P previousEnd = end;
        end = target.getTarget();
        if (!end.equals(previousEnd)) {
            resetRoute();
        }
        double maxWalk = speed.getNodeMovementLength(end);
        final Environment<T, P> env = getEnvironment();
        final Node<T> node = getNode();
        P curPos = env.getPosition(node);
        if (curPos.getDistanceTo(end) <= maxWalk) {
            final P destination = end;
            end = target.getTarget();
            resetRoute();
            return isAbsolute() ? destination : destination.minus(curPos);
        }
        if (route == null) {
            route = routing.computeRoute(curPos, end);
        }
        if (route.size() < 1) {
            resetRoute();
            return getDestination(curPos, end, maxWalk);
        }
        do {
            P target = route.getPoint(curStep);
            double toWalk = target.getDistanceTo(curPos);
            if (toWalk > maxWalk) {
                /*
                 * I can arrive at most at maxWalk
                 */
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
        return getDestination(curPos, end, maxWalk);
    }

    /**
     * @param current the current position of the node
     * @param target the target that should be reached
     * @param maxWalk how far the node can move
     * @return the position that the node reaches
     */
    protected abstract P getDestination(P current, P target, double maxWalk);

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
}
