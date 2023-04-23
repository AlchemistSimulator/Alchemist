/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.layers;

import it.unibo.alchemist.model.Layer;
import it.unibo.alchemist.model.Position;

/**
 * a Layer where the concentration is the same at every point in space.
 *
 * @param <P> position type
 * @param <T> concentration type
 */
public final class ConstantLayer<T, P extends Position<? extends P>> implements Layer<T, P> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final T level;

    /**
     * @param level
     *            the concentration
     */
    public ConstantLayer(final T level) {
        this.level = level;
    }

    @Override
    public T getValue(final P p) {
        return level;
    }

}
