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
import it.unibo.alchemist.utils.MapUtils;

/**
 * @param <T>
 */
public class MoveOnMap<T> extends AbstractConfigurableMoveNode<T> {

    /**
     * Minimum distance to walk per step in meters. Under this value, the
     * movement will become imprecise, due to errors in computation of the
     * distance between two points on the surface of the Earth.
     */
    public static final double MINIMUM_DISTANCE_WALKED = 1.0;
    private static final long serialVersionUID = 1L;

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
        super(environment, node, rt, tg, sp, true);
    }

    @Override
    public MapEnvironment<T> getEnvironment() {
        return (MapEnvironment<T>) super.getEnvironment();
    }

    @Override
    public MoveOnMap<T> cloneOnNewNode(final Node<T> n, final Reaction<T> r) {
        /*
         * Routing strategies can not be cloned at the moment.
         */
        throw new UnsupportedOperationException("Routing strategies can not be cloned.");
    }

    @Override
    protected Position getDestination(final Position current, final Position target, final double maxWalk) {
        return MapUtils.getDestinationLocation(current, target, maxWalk);
    }

}
