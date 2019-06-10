/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.displacements;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

import java.util.stream.Stream;

/**
 * A single node in a single point.
 *
 * @param <P> position type
 */
public final class Point<P extends Position<? extends P>> implements Displacement<P> {

    private final double x, y;
    private final Environment<?, P> pm;

    /**
     * @param pm
     *            The {@link Environment}
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     */
    public Point(final Environment<?, P> pm, final double x, final double y) {
        this.x = x;
        this.y = y;
        this.pm = pm;
    }

    @Override
    public Stream<P> stream() {
        return Stream.of(pm.makePosition(x, y));
    }

}
