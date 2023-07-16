/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps.linkingrules;

import it.unibo.alchemist.model.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.maps.MapEnvironment;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.linkingrules.AbstractLocallyConsistentLinkingRule;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param <T> concentration type
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
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, GeoPosition> environment) {
        if (environment instanceof MapEnvironment) {
            final MapEnvironment<T, ?, ?> menv = (MapEnvironment<T, ?, ?>) environment;
            final Stream<Node<T>> stream = menv.getNodesWithinRange(center, range).parallelStream();
            final List<Node<T>> filtered = stream
                    .filter(node -> menv.computeRoute(center, node).length() < range)
                    .collect(Collectors.toList());
            return Neighborhoods.make(menv, center, filtered);
        }
        return Neighborhoods.make(environment, center);
    }

}
