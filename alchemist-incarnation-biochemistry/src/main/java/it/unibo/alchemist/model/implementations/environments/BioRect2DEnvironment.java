/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments;

import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.capabilities.CellularBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param minX minimum X coordinate
     * @param maxX maximum X coordinate
     * @param minY minimum Y coordinate
     * @param maxY maximum Y coordinate
     */
    public BioRect2DEnvironment(final double minX, final double maxX, final double minY, final double maxY) {
        super(
            SupportedIncarnations.<Double, Euclidean2DPosition>get("biochemistry")
                .orElseThrow(() -> new IllegalStateException())
        );
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
     */
    public BioRect2DEnvironment() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Override
    protected final Euclidean2DPosition next(final double ox, final double oy, final double nx, final double ny) {
        double x, y;
        if (nx > maxX) {
            x = maxX;
        } else if (nx < minX) {
            x = minX;
        } else {
            x = nx;
        }
        if (ny > maxY) {
            y = maxY;
        } else if (ny < minY) {
            y = minY;
        } else {
            y = ny;
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
    public final void moveNode(final Node<Double> node, final Euclidean2DPosition direction) {
        if (node.asCapabilityOrNull(CellularBehavior.class) != null) {
            super.moveNode(node, direction);
            final Node<Double> nodeToMove = node;
            final Neighborhood<Double> neigh = getNeighborhood(nodeToMove);
            final Map<Junction, Map<Node<Double>, Integer>> jun = nodeToMove
                    .asCapability(CellularBehavior.class).getJunctions();
            jun.forEach((key, value) -> value.forEach((key1, value1) -> {
                if (!neigh.contains(key1)) {
                    // there is a junction that links a node which isn't in the neighborhood after the movement
                    for (int i = 0; i < value1; i++) {
                        nodeToMove.asCapability(CellularBehavior.class).removeJunction(key, key1);
                        key1.asCapability(CellularBehavior.class).removeJunction(key.reverse(), nodeToMove);
                    }
                }
            }));
        }
    }

}
