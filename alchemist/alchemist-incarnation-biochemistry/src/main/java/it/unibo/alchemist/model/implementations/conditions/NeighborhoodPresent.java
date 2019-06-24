/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * A condition is valid if the node has a neighborhood, formally if the node has at least one node 
 * connected by a linking rule.
 *
 * @param <T> The concentration type.
 */
public final class NeighborhoodPresent<T> extends AbstractNeighborCondition<T> {

    private static final long serialVersionUID = 689059297366332946L;

    /**
     * Create the condition.
     * @param node the node
     * @param environment the current environment.
     */
    public NeighborhoodPresent(final Environment<T, ?> environment, final Node<T> node) {
        super(environment, node);
    }

    @Override
    public NeighborhoodPresent<T> cloneCondition(final Node<T> n, final Reaction<T> r) {
        return new NeighborhoodPresent<>(getEnvironment(), n);
    }

    @Override
    protected double getNeighborPropensity(final Node<T> neighbor) {
        // to be eligible (p = 1) a neighbor just needs to be instance of CellNode
        return neighbor instanceof CellNode ? 1d : 0d;
    }

    @Override
    public boolean isValid() {
        return getEnvironment().getNeighborhood(getNode()).getNeighbors().stream()
                .anyMatch(n -> n instanceof CellNode);
    }

    @Override
    public String toString() {
        return " node has a neighbor ";
    }
}
