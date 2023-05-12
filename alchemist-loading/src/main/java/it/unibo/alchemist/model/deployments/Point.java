/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.deployments;

import it.unibo.alchemist.model.Deployment;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A single node in a single point.
 *
 * @param <P> position type
 */
public final class Point<P extends Position<? extends P>> implements Deployment<P> {

    private final double x, y;
    private final BiFunction<Double, Double, P> positionMaker;

    /**
     * @param environment
     *            The {@link Environment}
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     */
    public Point(final Environment<?, P> environment, final double x, final double y) {
        this.x = x;
        this.y = y;
        positionMaker = environment::makePosition;
    }

    @Override
    public Stream<P> stream() {
        return Stream.of(positionMaker.apply(x, y));
    }

}
