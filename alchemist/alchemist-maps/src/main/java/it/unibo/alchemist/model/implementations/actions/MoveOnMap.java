/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;
import it.unibo.alchemist.model.interfaces.IRoute;
import it.unibo.alchemist.utils.MapUtils;

/**
 * @param <T>
 */
public class MoveOnMap<T> extends AbstractMoveNode<T> {

    /**
     * Minimum distance to walk per step in meters. Under this value, the
     * movement will become imprecise, due to errors in computation of the
     * distance between two points on the surface of the Earth.
     */
    public static final double MINIMUM_DISTANCE_WALKED = 1.0;
    private static final long serialVersionUID = -2268285113653315764L;
    private Position end;
    private IRoute route;
    private int curStep;
    private final RoutingStrategy<T> routeStrategy;
    private final SpeedSelectionStrategy<T> speedStrategy;
    private final TargetSelectionStrategy<T> targetStrategy;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param rt the {@link RoutingStrategy}
     * @param sp
     *            the {@link SpeedSelectionStrategy}
     * @param tg
     *            {@link TargetSelectionStrategy}
     */
    public MoveOnMap(final MapEnvironment<T> environment, final Node<T> node, final RoutingStrategy<T> rt, final SpeedSelectionStrategy<T> sp, final TargetSelectionStrategy<T> tg) {
        super(environment, node, true);
        routeStrategy = rt;
        speedStrategy = sp;
        targetStrategy = tg;
    }

    @Override
    public MapEnvironment<T> getEnvironment() {
        return (MapEnvironment<T>) super.getEnvironment();
    }

    @Override
    public Position getNextPosition() {
        final Position previousEnd = end;
        end = targetStrategy.getNextTarget();
        if (!end.equals(previousEnd)) {
            resetRoute();
        }
        double maxWalk = speedStrategy.getCurrentSpeed(end);
        final MapEnvironment<T> env = getEnvironment();
        final Node<T> node = getNode();
        Position curPos = env.getPosition(node);
        if (curPos.getDistanceTo(end) <= maxWalk) {
            final Position destination = end;
            end = targetStrategy.getNextTarget();
            resetRoute();
            return destination;
        }
        if (route == null) {
            route = routeStrategy.computeRoute(curPos, end);
        }
        if (route.getPointsNumber() < 1) {
            resetRoute();
            return MapUtils.getDestinationLocation(curPos, end, maxWalk);
        }
        Position target = null;
        double toWalk;
        do {
            target = route.getPoint(curStep);
            toWalk = target.getDistanceTo(curPos);
            if (toWalk > maxWalk) {
                return MapUtils.getDestinationLocation(curPos, target, maxWalk);
            }
            curStep++;
            maxWalk -= toWalk;
            curPos = target;
        } while (curStep != route.getPointsNumber());
        /*
         * I've followed the whole route
         */
        resetRoute();
        target = end;
        return MapUtils.getDestinationLocation(curPos, target, maxWalk);
    }

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
    protected final IRoute getCurrentRoute() {
        return route;
    }

    @Override
    public MoveOnMap<T> cloneOnNewNode(final Node<T> n, final Reaction<T> r) {
        /*
         * Routing strategies can not be cloned at the moment.
         */
        throw new UnsupportedOperationException("Routing strategies can not be cloned.");
    }

}
