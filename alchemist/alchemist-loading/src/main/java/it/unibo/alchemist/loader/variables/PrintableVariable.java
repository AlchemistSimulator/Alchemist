/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.loader.variables;

import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * A variable stub, with a default {@link #toString()} method.
 *
 * @param <V>
 */
public abstract class PrintableVariable<V extends Serializable> implements Variable<V> {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return '[' + stream().map(Object::toString).collect(Collectors.joining(",")) + ']';
    }

}
