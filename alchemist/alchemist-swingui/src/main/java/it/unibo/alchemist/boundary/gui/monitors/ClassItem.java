/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.monitors;

import java.io.Serializable;

/**
 * A simple {@link Class} wrapper exposing a better {@link Object#toString()}.
 *
 * @param <E>
 */
public final class ClassItem<E> implements Serializable {

    private static final long serialVersionUID = 3274105941480613159L;
    private final Class<E> clazz;

    /**
     * @param clazz
     *            the class
     */
    public ClassItem(final Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ClassItem && clazz.equals(((ClassItem<?>) obj).clazz);
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    @Override
    public String toString() {
        return clazz.getSimpleName();
    }

    /**
     * @return the wrapped {@link Class}
     */
    public Class<E> getPayload() {
        return clazz;
    }

}
