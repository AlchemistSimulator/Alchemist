/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.movestrategies.routing.OnStreets;
import it.unibo.alchemist.model.implementations.movestrategies.speed.RoutingTraceDependantSpeed;
import it.unibo.alchemist.model.implementations.movestrategies.target.FollowTrace;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Vehicle;

/**
 * A walker that follows a trace. The trace is mandatory.
 * 
 * @param <T> Concentration Time
 */
public class GPSTraceWalker<T> extends MoveOnMapWithGPS<T> {

    private static final long serialVersionUID = -6495138719085165782L;

    /**
     * 
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param path
     *            resource(file, directory, ...) with GPS trace
     * @param cycle
     *            true if the traces have to be distributed cyclically
     * @param normalizer
     *            name of the class that implement the strategy to normalize the
     *            time
     * @param normalizerArgs
     *            Args to build normalize
     */
    public GPSTraceWalker(final MapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final String path, final boolean cycle, final String normalizer, final Object... normalizerArgs) {
        super(environment, node,
                new OnStreets<>(environment, Vehicle.FOOT),
                new RoutingTraceDependantSpeed<>(environment, node, reaction, Vehicle.FOOT),
                new FollowTrace(reaction),
                path, cycle, normalizer, normalizerArgs);
    }

    private GPSTraceWalker(final MapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final GPSTrace trace) {
        super(environment, node,
                new OnStreets<>(environment, Vehicle.FOOT),
                new RoutingTraceDependantSpeed<>(environment, node, reaction, Vehicle.FOOT),
                new FollowTrace(reaction),
                trace);
    }

    @Override
    public GPSTraceWalker<T> cloneAction(final Node<T> n, final Reaction<T> r) {
        return new GPSTraceWalker<>(getEnvironment(), n, r, getTrace());
    }

}
