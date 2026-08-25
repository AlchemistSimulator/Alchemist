/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;

import javax.annotation.Nonnull;

/**
 * Removes the current node from the environment.
 *
 * @param <T> concentration type
 */
public final class RemoveNode<T> extends AbstractAction<T> {

    private final Environment<T, ?> environment;

    /**
     * @param environment the current environment
     * @param node the node for this action
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public RemoveNode(final Environment<T, ?> environment, final Node<T> node) {
        super(node);
        this.environment = environment;
    }

    @Override
    public void execute() {
        environment.removeNode(getNode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Remove node " + getNode().getId();
    }

    /**
     * @return the current environment
     */
    private Environment<T, ?> getEnvironment() {
        return environment;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public RemoveNode<T> cloneAction(@Nonnull final Node<T> node, @Nonnull final NodeReaction<T> reaction) {
        return new RemoveNode<>(getEnvironment(), node);
    }

}
