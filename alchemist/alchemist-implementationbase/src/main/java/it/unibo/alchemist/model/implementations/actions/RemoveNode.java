/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * @param <T> concentration type
 */
public class RemoveNode<T> extends AbstractAction<T> {

    /**
     * 
     */
    private static final long serialVersionUID = -7358217984854060148L;
    private final Environment<T, ?> env;

    /**
     * @param environment the current environment
     * @param node the node for this action
     */
    public RemoveNode(final Environment<T, ?> environment, final Node<T> node) {
        super(node);
        env = environment;
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public void execute() {
        env.removeNode(getNode());
    }

    @Override
    public String toString() {
        return "Remove node " + getNode().getId();
    }

    /**
     * @return the current environment
     */
    protected Environment<T, ?> getEnvironment() {
        return env;
    }

    @Override
    public RemoveNode<T> cloneAction(final Node<T> n, final Reaction<T> r) {
        return new RemoveNode<T>(getEnvironment(), n);
    }

}
