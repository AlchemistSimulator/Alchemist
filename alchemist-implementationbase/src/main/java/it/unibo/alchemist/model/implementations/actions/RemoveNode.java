/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Dependency;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;

/**
 * Removes the current node from the environment.
 *
 * @param <T> concentration type
 */
public final class RemoveNode<T> extends AbstractAction<T> {

    /**
     * 
     */
    private static final long serialVersionUID = -7358217984854060148L;
    private final Environment<T, ?> environment;

    /**
     * @param environment the current environment
     * @param node the node for this action
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public RemoveNode(final Environment<T, ?> environment, final Node<T> node) {
        super(node);
        this.environment = environment;
        declareDependencyTo(Dependency.MOVEMENT);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
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
    protected Environment<T, ?> getEnvironment() {
        return environment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RemoveNode<T> cloneAction(final Node<T> node, final Reaction<T> reaction) {
        return new RemoveNode<>(getEnvironment(), node);
    }

}
