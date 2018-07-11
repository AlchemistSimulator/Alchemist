/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

import org.apache.commons.math3.util.FastMath;
import org.danilopianini.util.FlexibleQuadTree;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;

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

    @Override
    public final Euclidean2DPosition sumVectors(final Euclidean2DPosition p1, final Euclidean2DPosition p2) {
        return p1.add(p2);
    }

}
