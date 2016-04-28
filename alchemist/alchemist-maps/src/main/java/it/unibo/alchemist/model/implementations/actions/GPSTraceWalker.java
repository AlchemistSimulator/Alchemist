/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.strategies.routing.OnStreets;
import it.unibo.alchemist.model.implementations.strategies.speed.RoutingTraceDependantSpeed;
import it.unibo.alchemist.model.implementations.strategies.target.FollowTrace;
import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Vehicle;

/**
 * A walker that follows a trace. The trace is mandatory.
 * 
 * @param <T> Concentration Time
 */
public class GPSTraceWalker<T> extends MoveOnMap<T> {

    private static final long serialVersionUID = -6495138719085165782L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     */
    public GPSTraceWalker(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction) {
        super(environment, node,
                new OnStreets<>(environment, Vehicle.FOOT),
                new RoutingTraceDependantSpeed<>(environment, node, reaction, Vehicle.FOOT),
                new FollowTrace<>(environment, node, reaction));
    }

    @Override
    public GPSTraceWalker<T> cloneOnNewNode(final Node<T> n, final Reaction<T> r) {
        return new GPSTraceWalker<>(getEnvironment(), n, r);
    }

}
