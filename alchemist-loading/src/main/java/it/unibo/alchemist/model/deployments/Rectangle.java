/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.deployments;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nonnull;

/**
 * @param <P> position type
 */
public class Rectangle<P extends Position<? extends P>> extends AbstractRandomDeployment<P> {

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    /**
     * @param environment
     *            the {@link Environment}
     * @param randomGenerator
     *            the {@link RandomGenerator}
     * @param nodes
     *            the number of nodes
     * @param x
     *            x start point
     * @param y
     *            y start point
     * @param sizeX
     *            x size
     * @param sizeY
     *            y size
     */
    public Rectangle(
        final Environment<?, P> environment,
        final RandomGenerator randomGenerator,
        final int nodes,
        final double x,
        final double y,
        final double sizeX,
        final double sizeY
    ) {
        super(environment, randomGenerator, nodes);
        this.x = x;
        this.y = y;
        this.width = sizeX;
        this.height = sizeY;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected P indexToPosition(final int i) {
        return makePosition(randomDouble(x, x + width), randomDouble(y, y + height));
    }

    /**
     * @return start x
     */
    protected double getX() {
        return x;
    }

    /**
     * @return start y
     */
    protected double getY() {
        return y;
    }

    /**
     * @return width
     */
    protected double getWidth() {
        return width;
    }

    /**
     * @return height
     */
    protected double getHeight() {
        return height;
    }

}
