/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * @param <T>
 *
 */
public abstract class AbstractLocalAction<T> extends AbstractAction<T> {

    private static final long serialVersionUID = -2347988094066090756L;

    /**
     * @param node the local node
     */
    protected AbstractLocalAction(final Node<T> node) {
        super(node);
    }

    @Override
    public final Context getContext() {
        return Context.LOCAL;
    }

}
