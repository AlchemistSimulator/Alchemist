/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Represents an action on a neighbor.
 * @param <T> the concentration type.
 */
public abstract class AbstractNeighborAction<T> extends AbstractRandomizableAction<T> {

    private static final long serialVersionUID = -2287346030993830896L;
    private final Environment<T, ?> env;
    private final Node<T> node;

    /**
     * 
     * @param node the current node
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    protected AbstractNeighborAction(final Node<T> node, final Environment<T, ?> environment, final RandomGenerator randomGenerator) {
        super(node, randomGenerator);
        this.node = node;
        env = environment;
    }

    @Override
    public abstract AbstractNeighborAction<T> cloneAction(Node<T> node, Reaction<T> reaction);

    /**
     * Execute the action on a random neighbor if the node has a neighborhood. Otherwise do nothing.
     */
    @Override
    public void execute() {
        final Neighborhood<T> neighborhood = env.getNeighborhood(node);
        if (!neighborhood.isEmpty()) {
            execute(neighborhood.getNeighborByNumber(getRandomGenerator().nextInt(neighborhood.size())));
        }
    }

    /**
     * Execute the action on the given target node.
     * NOTE, it is NOT guaranteed that this method checks if the target node is in the actual neighborhood 
     * of the node.
     * @param targetNode the node where the action will be execute
     */
    public abstract void execute(Node<T> targetNode);

    @Override
    public final Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * @return exposes the {@link Environment} to subclasses
     */
    protected final Environment<T, ?> getEnvironment() {
        return env;
    }

}
