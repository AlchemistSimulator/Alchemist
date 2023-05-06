/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.variables;

import it.unibo.alchemist.model.loading.Variable;

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
