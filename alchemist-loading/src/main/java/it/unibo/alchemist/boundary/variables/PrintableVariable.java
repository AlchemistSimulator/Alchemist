/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.variables;

import it.unibo.alchemist.boundary.Variable;

import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * A variable stub, with a default {@link #toString()} method.
 *
 * @param <V> value type of the variable
 */
public abstract class PrintableVariable<V extends Serializable> implements Variable<V> {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return '[' + stream().map(Object::toString).collect(Collectors.joining(",")) + ']';
    }

}
