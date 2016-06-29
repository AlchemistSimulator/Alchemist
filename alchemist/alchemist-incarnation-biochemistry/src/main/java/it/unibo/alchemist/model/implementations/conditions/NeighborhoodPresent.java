/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * A condition is valid if the node has a neighborhood, formally if the node has at least one node 
 * connected by a linking rule.
 *
 * @param <T> The concentration type.
 */
public class NeighborhoodPresent<T> extends AbstractCondition<T> {

    private static final long serialVersionUID = 689059297366332946L;
    private final Environment<T> env;

    /**
     * Create the condition.
     * @param node the node
     * @param environment the current environment.
     */
    public NeighborhoodPresent(final Node<T> node, final Environment<T> environment) {
        super(node);
        env = environment;
    }

    @Override
    public NeighborhoodPresent<T> cloneOnNewNode(final Node<T> n) {
        return new NeighborhoodPresent<>(n, env);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityConditioning() {
        return isValid() ? 1d : 0d;
    }

    @Override
    public boolean isValid() {
        return !env.getNeighborhood(getNode()).isEmpty();
    }

    @Override
    public String toString() {
        return " node has a neighbor ";
    }

}
