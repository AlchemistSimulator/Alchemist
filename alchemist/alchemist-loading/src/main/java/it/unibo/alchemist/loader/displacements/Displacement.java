/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.displacements;

import java.util.Iterator;
import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * @param <P>
 */
@FunctionalInterface
public interface Displacement<P extends Position<? extends P>> extends Iterable<P> {

    /**
     * @return a {@link Stream} over the positions of this {@link Displacement}
     */
    Stream<P> stream();

    @Override
    default Iterator<P> iterator() {
        return stream().iterator();
    }

}
