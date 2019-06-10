/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;

/**
 * @param <T>
 */
public class Continuous2DEnvironment<T> extends Abstract2DEnvironment<T, Euclidean2DPosition> {


    private static final long serialVersionUID = 1L;

    @Override
    public final Euclidean2DPosition makePosition(final Number... coordinates) {
        if (coordinates.length != 2) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " can only get used with 2-dimensional positions.");
        }
        return new Euclidean2DPosition(coordinates[0].doubleValue(), coordinates[1].doubleValue());
    }

}
