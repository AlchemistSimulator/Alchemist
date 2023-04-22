/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;

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
    public NeighborhoodPresent<T> cloneCondition(final Node<T> node, final Reaction<T> r) {
        return new NeighborhoodPresent<>(getEnvironment(), node);
    }

    @Override
    protected double getNeighborPropensity(final Node<T> neighbor) {
        // to be eligible (p = 1) a neighbor just needs to be instance of CellNode
        return neighbor.asPropertyOrNull(CellProperty.class) != null ? 1d : 0d;
    }

    @Override
    public boolean isValid() {
        return getEnvironment().getNeighborhood(getNode()).getNeighbors().stream()
                .anyMatch(n -> n.asPropertyOrNull(CellProperty.class) != null);
    }

    @Override
    public String toString() {
        return " node has a neighbor ";
    }
}
