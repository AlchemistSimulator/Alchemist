/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.euclidean2d.environments.LimitedContinuos2D;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 */
public class BioRect2DEnvironment extends LimitedContinuos2D<Double> {

    private static final long serialVersionUID = -2952112972706738682L;
    private static final Logger L = LoggerFactory.getLogger(BioRect2DEnvironment.class);

    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    /**
     * Builds a BioRect2DEnvironment with given bounds.
     * @param incarnation the current {@link BiochemistryIncarnation}
     * @param minX minimum X coordinate
     * @param maxX maximum X coordinate
     * @param minY minimum Y coordinate
     * @param maxY maximum Y coordinate
     */
    public BioRect2DEnvironment(
        final BiochemistryIncarnation incarnation,
        final double minX,
        final double maxX,
        final double minY,
        final double maxY
    ) {
        super(incarnation);
        if (maxX <= minX || maxY <= minY) {
            L.warn("A maximum bound for this environment is greather than the correspoding minimum bound. "
                    + "Falling back to -1, 1 for all bounds");
            this.minX = -1;
            this.maxX = 1;
            this.minY = -1;
            this.maxY = 1;
        } else {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    /**
     * Builds a BioRect2DEnvironment with infinite bounds.
     *
     * @param incarnation the current {@link BiochemistryIncarnation}
     */
    public BioRect2DEnvironment(final BiochemistryIncarnation incarnation) {
        this(
            incarnation,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY
        );
    }

    @Override
    protected final Euclidean2DPosition next(final double ox, final double oy, final double nx, final double ny) {
        double x, y;
        if (nx > maxX) {
            x = maxX;
        } else {
            x = Math.max(nx, minX);
        }
        if (ny > maxY) {
            y = maxY;
        } else {
            y = Math.max(ny, minY);
        }
        return new Euclidean2DPosition(x, y);
    }

    @Override
    protected final boolean isAllowed(final Euclidean2DPosition p) {
        return p.getX() < maxX && p.getX() > minX
                && p.getY() < maxY && p.getY() > minY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void moveNode(final Node<Double> node, @Nonnull final Euclidean2DPosition direction) {
        if (node.asPropertyOrNull(CellProperty.class) != null) {
            super.moveNode(node, direction);
            final Neighborhood<Double> neigh = getNeighborhood(node);
            final Map<Junction, Map<Node<Double>, Integer>> jun = node
                    .asProperty(CellProperty.class).getJunctions();
            jun.forEach((key, value) -> value.forEach((key1, value1) -> {
                if (!neigh.contains(key1)) {
                    // there is a junction that links a node which isn't in the neighborhood after the movement
                    for (int i = 0; i < value1; i++) {
                        node.asProperty(CellProperty.class).removeJunction(key, key1);
                        key1.asProperty(CellProperty.class).removeJunction(key.reverse(), node);
                    }
                }
            }));
        }
    }

}
