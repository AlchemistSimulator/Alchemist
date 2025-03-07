/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.nodes;

import it.unibo.alchemist.model.Environment;

import java.io.Serial;

/**
 * Generic node for testing purposes.
 */
public final class TestNode extends GenericNode<Object> {

    @Serial
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
