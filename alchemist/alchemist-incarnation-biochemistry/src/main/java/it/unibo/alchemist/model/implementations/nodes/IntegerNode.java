/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes;

import it.unibo.alchemist.model.interfaces.Environment;

/**
 */
public abstract class IntegerNode extends AbstractNode<Integer> {

    private static final long serialVersionUID = -1064026943504464379L;

    /**
     * Create a new integer node.
     * @param env the environment
     */
    public IntegerNode(final Environment<Integer, ?> env) {
        super(env);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer createT() {
        return 0;
    }

}
