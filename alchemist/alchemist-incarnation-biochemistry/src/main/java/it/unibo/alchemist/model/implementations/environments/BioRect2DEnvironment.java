/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors

 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.implementations.nodes.CellNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.ICellNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 */
public class BioRect2DEnvironment extends LimitedContinuos2D<Double> {

    private static final long serialVersionUID = -2952112972706738682L;
    private static final Logger L = LoggerFactory.getLogger(BioRect2DEnvironment.class);

    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    /**
     * Builds a BioRect2DEnvironment with given bounds.
     * @param minX minimum X coordinate
     * @param maxX maximum X coordinate
     * @param minY minimum Y coordinate
     * @param maxY maximum Y coordinate
     */
    public BioRect2DEnvironment(final double minX, final double maxX, final double minY, final double maxY) {
        if (maxX <= minX || maxY <= minY) {
            L.warn("A maximum bound for this environment is greather than the correspoding minimum bound. Falling back to -1, 1 for all bounds");
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
        minX = Double.MIN_VALUE;
        maxX = Double.MAX_VALUE;
        minY = Double.MIN_VALUE;
        maxY = Double.MAX_VALUE;
    }

    @Override
    protected Position next(final double ox, final double oy, final double nx, final double ny) {
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
        return new Continuous2DEuclidean(x, y);
    }

    @Override
    protected boolean isAllowed(final Position p) {
        return (p.getCoordinate(0) < maxX && p.getCoordinate(0) > minX 
                && p.getCoordinate(1) < maxY && p.getCoordinate(1) > minY);
    }

    @Override
    public void moveNode(final Node<Double> node, final Position direction) {
        if (node instanceof CellNode) {
            super.moveNode(node, direction);
            final Neighborhood<Double> neigh = getNeighborhood(node);
            final Map<Junction, Map<ICellNode, Integer>> jun = ((CellNode) node).getJunctions();
            jun.entrySet().stream().forEach(e -> e.getValue().entrySet().forEach(e2 -> {
                if (!neigh.contains(e2.getKey())) { // there is a junction that links a node which isn't in the neighborhood after the movement
                   ((CellNode) node).removeJunction(e.getKey(), e2.getKey());
                   e2.getKey().removeJunction(e.getKey().reverse(), (ICellNode) node);
                }
            }));
        }
    }

}