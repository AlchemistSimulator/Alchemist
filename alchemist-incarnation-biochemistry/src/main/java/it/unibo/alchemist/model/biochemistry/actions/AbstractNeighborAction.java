/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.util.Iterables;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nonnull;

/**
 * Represents an action on a neighbor.
 *
 * @param <T> the concentration type.
 */
public abstract class AbstractNeighborAction<T> extends AbstractRandomizableAction<T> {

    private final Environment<T, ?> environment;

    /**
     * @param node the current node
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    protected AbstractNeighborAction(
            final Node<T> node,
            final Environment<T, ?> environment,
            final RandomGenerator randomGenerator
    ) {
        super(node, randomGenerator);
        this.environment = environment;
    }

    @Nonnull
    @Override
    public abstract AbstractNeighborAction<T> cloneAction(
        @Nonnull Node<T> newNode,
        @Nonnull NodeReaction<T> newReaction
    );

    /**
     * Execute the action on a random neighbor if the node has a neighborhood. Otherwise do nothing.
     */
    @Override
    public void execute() {
        final Neighborhood<T> currentNeighborhood = getEnvironment().getNeighborhood(getNode()).getCurrent();
        if (!currentNeighborhood.isEmpty()) {
            execute(Iterables.INSTANCE.randomElement(currentNeighborhood, getRandomGenerator()));
        }
    }

    /**
     * Execute the action on the given target node.
     * NOTE, it is NOT guaranteed that this method checks if the target node is in the actual neighborhood
     * of the node.
     *
     * @param targetNode the node where the action will be execute
     */
    public abstract void execute(Node<T> targetNode);

    @Nonnull
    @Override
    public final Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * @return exposes the {@link Environment} to subclasses
     */
    protected final Environment<T, ?> getEnvironment() {
        return environment;
    }

}
