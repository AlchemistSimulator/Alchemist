/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * @param <T>
 */
public final class LinkNodesWithinRoutingRange<T> extends AbstractLocallyConsistentLinkingRule<T, GeoPosition> {

    private static final long serialVersionUID = 726751817489962367L;
    private final double range;

    /**
     * @param r range
     */
    public LinkNodesWithinRoutingRange(final double r) {
        range = r;
    }

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, GeoPosition> env) {
        if (env instanceof MapEnvironment<?>) {
            final MapEnvironment<T> menv = (MapEnvironment<T>) env;
            final Stream<Node<T>> stream = menv.getNodesWithinRange(center, range).parallelStream();
            final List<Node<T>> filtered = stream.filter(node -> menv.computeRoute(center, node).length() < range).collect(Collectors.toList());
            return Neighborhoods.make(menv, center, filtered);
        }
        return Neighborhoods.make(env, center);
    }

}
