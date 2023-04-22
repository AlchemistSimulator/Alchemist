/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.deployments;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Position;
import org.apache.commons.math3.random.RandomGenerator;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

/**
 * @param <P> {@link Position} type
 */
public final class Circle<P extends Position<? extends P>> extends AbstractRandomDeployment<P> {

    private final double centerx, centery, radius;

    /**
     * @param pm
     *            the {@link Environment}
     * @param rand
     *            the {@link RandomGenerator}
     * @param nodes
     *            the number of nodes
     * @param centerX
     *            the center x of the circle
     * @param centerY
     *            the center y of the circle
     * @param radius
     *            the radius of the circle
     */
    public Circle(final Environment<?, P> pm,
            final RandomGenerator rand,
            final int nodes,
            final double centerX, final double centerY, final double radius) {
        super(pm, rand, nodes);
        this.centerx = centerX;
        this.centery = centerY;
        this.radius = radius;
    }

    @Override
    protected P indexToPosition(final int i) {
        final double angle = randomDouble(0, 2 * PI);
        final double rad = radius * sqrt(randomDouble());
        return makePosition(centerx + rad * cos(angle), centery + rad * sin(angle));
    }

}
