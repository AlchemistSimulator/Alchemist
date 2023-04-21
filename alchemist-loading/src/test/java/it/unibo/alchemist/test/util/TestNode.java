/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test.util;

import it.unibo.alchemist.model.implementations.nodes.GenericNode;
import it.unibo.alchemist.model.Environment;

/**
 * Generic node for testing purposes.
 */
public final class TestNode extends GenericNode<Object> {

    private static final long serialVersionUID = 1L;

    /**
     * @param environment the environment
     */
    public TestNode(final Environment<Object, ?> environment) {
        super(environment);
    }

    @Override
    protected Object createT() {
        return new Object();
    }

}
