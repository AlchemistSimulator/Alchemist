/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.stream.Stream;

import com.google.common.base.Optional;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * Implements a limited environment supporting cells with a defined shape, 
 * avoiding any overlapping among them.
 * 
 * 
 */
public final class BioRect2DEnvironmentNoOverlap extends BioRect2DEnvironment implements EnvironmentSupportingDeformableCells<Euclidean2DPosition> {

    private static final long serialVersionUID = 1L;
    private static final Logger L = LoggerFactory.getLogger(BioRect2DEnvironmentNoOverlap.class);
    private Optional<CellWithCircularArea<Euclidean2DPosition>> biggestCellWithCircularArea = Optional.absent();
    private Optional<CircularDeformableCell<Euclidean2DPosition>> biggestCircularDeformableCell = Optional.absent();

    /**
     * Returns an infinite {@link BioRect2DEnvironment}.
     */
    public BioRect2DEnvironmentNoOverlap() {
        super();
    }
    /**
     * Returns a limited rectangular {@link BioRect2DEnvironment}.
     * 
     * @param minX rectangle's min x value.
     * @param maxX rectangle's max x value.
     * @param minY rectangle's min y value.
     * @param maxY rectangle's max y value.
     */
    public BioRect2DEnvironmentNoOverlap(final double minX, final double maxX, final double minY, final double maxY) {
        super(minX, maxX, minY, maxY);
    }

    @Override
    protected boolean nodeShouldBeAdded(final Node<Double> node, final Euclidean2DPosition p) {
        final boolean isWithinLimits = super.nodeShouldBeAdded(node, p);
        if (isWithinLimits) {
            if (node instanceof CellWithCircularArea) {
                final CellWithCircularArea<Euclidean2DPosition> thisNode = (CellWithCircularArea<Euclidean2DPosition>) node;
                double range = getMaxDiameterAmongCellWithCircularShape();
                if (thisNode.getDiameter() > range) {
                    range = thisNode.getDiameter();
                }
                final double nodeRadius = thisNode.getRadius();
                return range <= 0
                        || getNodesWithinRange(p, range).stream()
                            .filter(n -> n instanceof CellWithCircularArea)
                            .map(n -> (CellWithCircularArea<Euclidean2DPosition>) n)
                            .noneMatch(n -> getPosition(n).getDistanceTo(p) < nodeRadius + n.getRadius());
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void moveNodeToPosition(final Node<Double> node, final Euclidean2DPosition newPos) {
        final double[] cur = getPosition(node).getCartesianCoordinates();
        final double[] np = newPos.getCartesianCoordinates();
        final Euclidean2DPosition nextWithinLimts = super.next(cur[0], cur[1], np[0], np[1]);
        if (node instanceof CellWithCircularArea) {
            final Euclidean2DPosition nextPos = findNearestFreePosition((CellWithCircularArea<Euclidean2DPosition>) node, new Euclidean2DPosition(cur[0], cur[1]), nextWithinLimts);
            super.moveNodeToPosition(node, nextPos);
        } else {
            super.moveNodeToPosition(node, nextWithinLimts);
        }
    }

    /*
     *  finds the first position, in requested direction (requestedPos - originalPos), that can be occupied by the cell.
     */
    private Euclidean2DPosition findNearestFreePosition(
            final CellWithCircularArea<Euclidean2DPosition> nodeToMove,
            final Euclidean2DPosition originalPos,
            final Euclidean2DPosition requestedPos) {
        // get the maximum range depending by cellular shape
        final double maxDiameter = getMaxDiameterAmongCellWithCircularShape();
        final double distanceToReq = originalPos.getDistanceTo(requestedPos);
        if (maxDiameter == 0d || distanceToReq == 0) {
            return requestedPos;
        }
        final double distanceToScan = distanceToReq + nodeToMove.getRadius() + (maxDiameter / 2);
        final double halfDistance = distanceToScan / 2;
        // compute position of the midpoint between originalPos and a point at distance distanceToScan
        final double rx = requestedPos.getX();
        final double ox = originalPos.getX();
        final double xVec = rx - ox;
        final double ry = requestedPos.getY();
        final double oy = originalPos.getY();
        final double yVec = ry - oy;
        final double module = FastMath.sqrt(FastMath.pow(xVec, 2) + FastMath.pow(yVec, 2));
        final double xVer = xVec / module;
        final double yVer = yVec / module;
        final double xVecToMid1 = xVer * halfDistance;
        final double yVecToMid1 = yVer * halfDistance;
        final Euclidean2DPosition vecToMid1 = new Euclidean2DPosition(xVecToMid1, yVecToMid1);
        final Euclidean2DPosition midPoint = originalPos.plus(vecToMid1);
        // compute optimum scanning range
        double range = FastMath.sqrt(FastMath.pow(halfDistance, 2) + FastMath.pow(maxDiameter, 2));
        final double newMaxDiameter = getNodesWithinRange(midPoint, range).stream()
                .parallel()
                .filter(n -> n instanceof CellWithCircularArea)
                .mapToDouble(n -> ((CellWithCircularArea<Euclidean2DPosition>) n).getDiameter())
                .max()
                .orElse(0);
        final double newDistanceToScan = distanceToReq + nodeToMove.getRadius() + newMaxDiameter / 2;
        final double newHalfDistance = newDistanceToScan / 2;
        final Euclidean2DPosition vecToMid2 = new Euclidean2DPosition(xVer * newHalfDistance, yVer * newHalfDistance);
        final Euclidean2DPosition newMidPoint = originalPos.plus(vecToMid2);
        range = FastMath.sqrt(FastMath.pow(newHalfDistance, 2) + FastMath.pow(newMaxDiameter, 2));
        return getNodesWithinRange(newMidPoint, range).stream()
                .filter(n -> !n.equals(nodeToMove) && n instanceof CellWithCircularArea)
                .map(n -> (CellWithCircularArea<Euclidean2DPosition>) n)
                .filter(n -> selectNodes(n, nodeToMove, getPosition(nodeToMove), requestedPos, xVer, yVer))
                .map(n -> getPositionIfNodeIsObstacle(nodeToMove, n, originalPos, oy, ox, ry, rx)) 
                .filter(Optional::isPresent) 
                .map(Optional::get)
                .min(Comparator.comparingDouble(p -> p.getDistanceTo(originalPos)))
                .orElse(requestedPos);
    }

    private boolean selectNodes(final CellWithCircularArea<Euclidean2DPosition> node,
            final CellWithCircularArea<Euclidean2DPosition> nodeToMove,
            final Euclidean2DPosition origin,
            final Euclidean2DPosition requestedPos,
            final double xVer, final double yVer) {
        // testing if node is between requested position and original position
        final Euclidean2DPosition nodePos = getPosition(node);
        final Euclidean2DPosition nodeOrientationFromOrigin = new Euclidean2DPosition(nodePos.getX() - origin.getX(), 
                nodePos.getY() - origin.getY());
        final double scalarProductResult1 = xVer * nodeOrientationFromOrigin.getX() + yVer * nodeOrientationFromOrigin.getY();
        // testing if node is near enough to requested position to be an obstacle
        final Euclidean2DPosition oppositeVersor = new Euclidean2DPosition(-xVer, -yVer);
        final Euclidean2DPosition nodeOrientationFromReq = new Euclidean2DPosition(nodePos.getX() - requestedPos.getX(), 
                nodePos.getY() - requestedPos.getY());
        final double scalarProductResult2 = oppositeVersor.getX() * nodeOrientationFromReq.getX() + oppositeVersor.getY() * nodeOrientationFromReq.getY();
        if (scalarProductResult2 <= 0) {
            return nodePos.getDistanceTo(requestedPos) < node.getRadius() + nodeToMove.getRadius()
                    && scalarProductResult1 >= 0;
        }
        return scalarProductResult1 >= 0;
    }

    // returns the Optional containing the position of the node, if it's an obstacle for movement
    private Optional<Euclidean2DPosition> getPositionIfNodeIsObstacle(
            final CellWithCircularArea<Euclidean2DPosition> nodeToMove, 
            final CellWithCircularArea<Euclidean2DPosition> node, 
            final Euclidean2DPosition originalPos, 
            final double yo,
            final double xo,
            final double yr,
            final double xr) {
        // original position 
        final Euclidean2DPosition possibleObstaclePosition = getPosition(node);
        // coordinates of original position, requested position and of node's position
        final double yn = possibleObstaclePosition.getY();
        final double xn = possibleObstaclePosition.getX();
        // cellular range
        final double cellRange = node.getRadius() + nodeToMove.getRadius();
        // compute intersection
        final double xIntersect;
        final double yIntersect;
        if (MathUtils.fuzzyEquals(yo, yr)) {
            yIntersect = yo;
            xIntersect = xn;
        } else if (MathUtils.fuzzyEquals(xo, xr)) {
            xIntersect = xo;
            yIntersect = yn;
        } else {
            // computes parameters of the straight line from original position to requested position
            final double m1 = (yo - yr) / (xo - xr);
            final double q1 = yo - m1 * xo;
            // computes parameter of straight line, perpendicular to the previous, passing through the cell
            final double m2 = -1 / m1;
            final double q2 = yn - m2 * xn;
            // compute intersection between this two straight lines
            xIntersect = (q2 - q1) / (m1 - m2);
            yIntersect = m2 * xIntersect + q2;
        }
        final Euclidean2DPosition intersection = new Euclidean2DPosition(xIntersect, yIntersect);
        // computes distance between the cell and the first straight line
        final double cat = intersection.getDistanceTo(possibleObstaclePosition);
        // if cat is bigger than cellRange, actual cell isn't an obstacle for the cellular movement
        if (cat >= cellRange) {
            // so returns an empty optional
            return Optional.absent();
        }
        // otherwise, compute the maximum practicable distance for the cell
        final double module =  FastMath.sqrt(FastMath.pow(yIntersect - yo, 2) + FastMath.pow(xIntersect - xo, 2));
        if (module == 0) { // if module == 0 the translation is 0, so return originalPos
            return Optional.of(originalPos);
        }
        // compute the versor relative to requested direction of cell movement
        final double cat2 = FastMath.sqrt(FastMath.pow(cellRange, 2) - FastMath.pow(cat, 2));
        final double distToSum  = originalPos.getDistanceTo(intersection) - cat2;
        final Euclidean2DPosition versor = new Euclidean2DPosition((xIntersect - xo) / module, (yIntersect - yo) / module);
        // computes vector representing the practicable movement
        final Euclidean2DPosition vectorToSum = new Euclidean2DPosition(distToSum * versor.getX(), distToSum * versor.getY());
        // returns the right position of the cell
        final Euclidean2DPosition result = originalPos.plus(vectorToSum);
        return Optional.of(result);
    }

    @Override
    protected void nodeAdded(final Node<Double> node, final Euclidean2DPosition position, final Neighborhood<Double> neighborhood) {
        super.nodeAdded(node, position, neighborhood);
        if (node instanceof CellWithCircularArea) {
            final CellWithCircularArea<Euclidean2DPosition> cell = (CellWithCircularArea<Euclidean2DPosition>) node;
            if (cell.getDiameter() > getMaxDiameterAmongCellWithCircularShape()) {
                biggestCellWithCircularArea = Optional.of(cell); 
            }
        }
        if (node instanceof CircularDeformableCell) {
            final CircularDeformableCell<Euclidean2DPosition> cell = (CircularDeformableCell<Euclidean2DPosition>) node;
            if (cell.getMaxDiameter() > getMaxDiameterAmongCircularDeformableCells()) {
                biggestCircularDeformableCell = Optional.of(cell); 
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void nodeRemoved(final Node<Double> node, final Neighborhood<Double> neighborhood) {
        if (node instanceof CellWithCircularArea) {
            if (biggestCircularDeformableCell.isPresent() && biggestCircularDeformableCell.get().equals(node)) {
                biggestCircularDeformableCell = getBiggest(CircularDeformableCell.class)
                        .transform(c -> (CircularDeformableCell<Euclidean2DPosition>) c);
            }
            if (biggestCellWithCircularArea.isPresent() && biggestCellWithCircularArea.get().equals(node)) {
                biggestCellWithCircularArea = getBiggest(CellWithCircularArea.class)
                        .transform(c -> (CellWithCircularArea<Euclidean2DPosition>) c);
            }
        }
    }

    private <C> Optional<C> getBiggest(final Class<C> cellClass) {
        final boolean isDeformable;
        if (cellClass.equals(CircularDeformableCell.class)) {
            isDeformable = true;
        } else if (cellClass.equals(CellWithCircularArea.class)) {
            isDeformable = false;
        } else {
            throw new UnsupportedOperationException("Input type must be CellWithCircuolarShape or CircularDeformableCell");
        }

        return getNodes().stream()
                .parallel()
                .flatMap(n -> cellClass.isInstance(n) ? Stream.of(cellClass.cast(n)) : Stream.empty())
                .max((c1, c2) -> {
                    final Method diameterToCompare;
                    try {
                        if (isDeformable) {
                            diameterToCompare = cellClass.getMethod("getMaxDiameter");
                        } else {
                            diameterToCompare = cellClass.getMethod("getDiameter");
                        }
                        if (diameterToCompare.getReturnType().equals(double.class)) {
                            try {
                                return Double.compare((double) diameterToCompare.invoke(c1), (double) diameterToCompare.invoke(c2));
                            } catch (IllegalAccessException e) {
                                L.error("Method not accessible");
                                return 0;
                            } catch (IllegalArgumentException e) {
                                L.error("Wrong parameter types, or wrong parameters' number");
                                return 0;
                            } catch (InvocationTargetException e) {
                                L.error("Invoked method throwed an exception");
                                return 0;
                            } catch (ExceptionInInitializerError e) {
                                L.error("Initialization failed");
                                return 0;
                            }
                        } else {
                            throw new ClassFormatException("Return type of method " + diameterToCompare.getName() + " should be double");
                        }
                    } catch (NoSuchMethodException e) {
                        L.error("Method " + (isDeformable ? "getMaxDiameter" : "getDiameter") + "not foung in class " + cellClass.getName());
                        return 0;
                    }
                })
                .map(Optional::of)
                .orElse(Optional.absent());
    }

    private double getMaxDiameterAmongCellWithCircularShape() {
        return getDiameterFromCell(biggestCellWithCircularArea);
    }

    @Override
    public double getMaxDiameterAmongCircularDeformableCells() {
        return getDiameterFromCell(biggestCircularDeformableCell);
    }

    private double getDiameterFromCell(final Optional<? extends CellWithCircularArea<Euclidean2DPosition>> biggest) {
        return biggest
                .transform(n -> n instanceof CircularDeformableCell
                        ? ((CircularDeformableCell<Euclidean2DPosition>) n).getMaxDiameter()
                        : n.getDiameter())
                .or(0d);
    }
        }
