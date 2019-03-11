/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.model.implementations.nodes;

import it.unibo.alchemist.model.interfaces.Environment;

/**
 */
public class DoubleNode extends AbstractNode<Double> {

    private static final long serialVersionUID = 3426243332828063314L;

    /**
     * Builds a new DoubleNode.
     * @param env the environment
     */
    public DoubleNode(final Environment<Double, ?> env) {
        super(env);
    }

    @Override
    protected final Double createT() {
        return 0d;
    }
}
