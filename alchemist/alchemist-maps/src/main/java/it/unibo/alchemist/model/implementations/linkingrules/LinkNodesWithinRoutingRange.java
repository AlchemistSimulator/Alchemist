/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unibo.alchemist.model.implementations.neighborhoods.CachedNeighborhood;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * @param <T>
 */
public class LinkNodesWithinRoutingRange<T> implements LinkingRule<T> {

    private static final long serialVersionUID = 726751817489962367L;
    private final Collection<Node<T>> emptyList = Collections.unmodifiableCollection(new ArrayList<Node<T>>(0));
    private final double range;

    /**
     * @param r range
     */
    public LinkNodesWithinRoutingRange(final double r) {
        range = r;
    }

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T> env) {
        if (env instanceof MapEnvironment<?>) {
            final MapEnvironment<T> menv = (MapEnvironment<T>) env;
            final Stream<Node<T>> stream = menv.getNodesWithinRange(center, range).parallelStream();
            final Collection<Node<T>> filtered = stream.filter(node -> menv.computeRoute(center, node).getDistance() < range).collect(Collectors.toList());
            return new CachedNeighborhood<>(center, filtered, menv);
        }
        return new CachedNeighborhood<>(center, emptyList, env);
    }

}
