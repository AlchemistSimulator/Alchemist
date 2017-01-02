/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Fake movement class, used only to trigger the neighborhood update.
 * 
 * @param <T>
 */
public class FakeMove<T> extends AbstractMoveNode<T> {

    private static final long serialVersionUID = 1774989279335172458L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     */
    public FakeMove(final Environment<T> environment, final Node<T> node) {
        super(environment, node, true);
    }

    @Override
    public Action<T> cloneAction(final Node<T> n, final Reaction<T> r) {
        return new FakeMove<>(getEnvironment(), n);
    }

    @Override
    public Position getNextPosition() {
        return getEnvironment().getPosition(getNode());
    }

}
