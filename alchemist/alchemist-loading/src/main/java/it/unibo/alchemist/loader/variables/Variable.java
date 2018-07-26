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
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A variable simulation value, that provides a range of values for batches, and
 * a default value for single-shot runs.
 *
 * @param <V>
 */
public interface Variable<V extends Serializable> extends Serializable, Iterable<V> {

    @Override
    default Iterator<V> iterator() {
        return stream().iterator();
    }

    /**
     * @return the number of different values this {@link Variable} may yield
     */
    default long steps() {
        return stream().count();
    };

    /**
     * @return the default value for this {@link Variable}
     */
    V getDefault();

    /**
     * @return a view of the values of this variable as {@link Stream}.
     */
    Stream<V> stream();

}
