/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Node;

/**
 * @param <T> concentration type
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
