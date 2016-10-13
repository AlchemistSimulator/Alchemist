package it.unibo.alchemist.model.implementations.environments;

import java.util.stream.Stream;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Optional;

import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Implements a limited environment supporting cells with a defined shape, 
 * avoiding any overlapping among them.
 * 
 * 
 */
public class BioRect2DEnvironmentNoOverlap extends BioRect2DEnvironment implements EnvironmentSupportingDeformableCells {

    private static final long serialVersionUID = 1L;
    private Optional<CellWithCircularArea> biggestCell = Optional.absent();
    private Optional<CircularDeformableCell> biggestDeformableCell = Optional.absent();

    /**
     * Returns an infinite BioRect2DEnviroment.
     */
    public BioRect2DEnvironmentNoOverlap() {
        super();
    }
    /**
     * Returns a limited rectangular BioRect2DEnviroment.
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
    protected boolean nodeShouldBeAdded(final Node<Double> node, final Position p) {
        final boolean isWithinLimits = super.nodeShouldBeAdded(node, p);
        if (isWithinLimits) {
            if (node instanceof CellWithCircularArea) {
                final CellWithCircularArea thisNode = (CellWithCircularArea) node;
                double range = getMaxDiameterAmongCells();
                if (thisNode.getDiameter() > range) {
                    range = thisNode.getDiameter();
                }
                final double nodeRadius = thisNode.getRadius();
                return isWithinLimits 
                        && !(getNodesWithinRange(p, range).stream()
                                .parallel()
                                .filter(n -> (n instanceof CellWithCircularArea) && (getPosition(n).getDistanceTo(p) < nodeRadius + ((CellWithCircularArea) n).getRadius()))
                                .findFirst()
                                .isPresent());
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void moveNodeToPosition(final Node<Double> node, final Position newPos) {
        final double[] cur = getPosition(node).getCartesianCoordinates();
        final double[] np = newPos.getCartesianCoordinates();
        final Position nextWithinLimts = super.next(cur[0], cur[1], np[0], np[1]);
        if (node instanceof CellWithCircularArea) {
            final Position nextPos = findNearestFreePosition((CellWithCircularArea) node, new Continuous2DEuclidean(cur[0], cur[1]), nextWithinLimts);
            super.moveNodeToPosition(node, nextPos);
        } else {
            super.moveNodeToPosition(node, nextWithinLimts);
        }
    }

    /*
     *  finds the first position, in requested direction (requestedPos - originalPos), that can be occupied by the cell.
     */
    private Position findNearestFreePosition(final CellWithCircularArea nodeToMove, final Position originalPos, final Position requestedPos) {
        // get the maximum range depending by cellular shape
        final double maxDiameter = getMaxDiameterAmongCells();
        final double distanceToReq = originalPos.getDistanceTo(requestedPos);
        if (maxDiameter == 0d || distanceToReq == 0) {
            return requestedPos;
        }
        final double distanceToScan = distanceToReq + nodeToMove.getRadius() + (maxDiameter / 2);
        final double halfDistance = (distanceToScan / 2);
        // compute position of the midpoint between originalPos and a point at distance distanceToScan
        final double rx = requestedPos.getCoordinate(0);
        final double ox = originalPos.getCoordinate(0);
        final double xVec = rx - ox;
        final double ry = requestedPos.getCoordinate(1);
        final double oy = originalPos.getCoordinate(1);
        final double yVec = ry - oy;
        final double module = FastMath.sqrt(FastMath.pow(xVec, 2) + FastMath.pow(yVec, 2));
        final double xVer = xVec / module;
        final double yVer = yVec / module;
        final double xVecToMid1 = xVer * halfDistance;
        final double yVecToMid1 = yVer * halfDistance;
        final Position vecToMid1 = new Continuous2DEuclidean(xVecToMid1, yVecToMid1);
        final Position midPoint = originalPos.sum(vecToMid1);
        // compute optimum scanning range
        double range = FastMath.sqrt(FastMath.pow(halfDistance, 2) + FastMath.pow(maxDiameter, 2));
        final double newMaxDiameter = getNodesWithinRange(midPoint, range).stream()
                .parallel()
                .filter(n -> n instanceof CellWithCircularArea)
                .mapToDouble(n -> ((CellWithCircularArea) n).getDiameter())
                .max()
                .orElse(0);
        final double newDistanceToScan = distanceToReq + nodeToMove.getRadius() + newMaxDiameter / 2;
        final double newHalfDistance = newDistanceToScan / 2;
        final Position vecToMid2 = new Continuous2DEuclidean(xVer * newHalfDistance, yVer * newHalfDistance);
        final Position newMidPoint = originalPos.sum(vecToMid2);
        range = FastMath.sqrt(FastMath.pow(newHalfDistance, 2) + FastMath.pow(newMaxDiameter, 2));
        return getNodesWithinRange(newMidPoint, range).stream()
                .parallel()
                .filter(n -> !n.equals(nodeToMove) && n instanceof CellWithCircularArea)
                .map(n -> (CellWithCircularArea) n)
                .filter(n -> selectNodes(n, nodeToMove, getPosition(nodeToMove), requestedPos, xVer, yVer))
                .map(n -> getPositionIfNodeIsObstacle(nodeToMove, n, originalPos, oy, ox, ry, rx)) 
                .filter(Optional::isPresent) 
                .map(Optional::get)
                .min((p1, p2) -> Double.compare(p1.getDistanceTo(originalPos), p2.getDistanceTo(originalPos)))
                .orElse(requestedPos);
    }

    private boolean selectNodes(final CellWithCircularArea node, final CellWithCircularArea nodeToMove, final Position origin, final Position requestedPos, final double xVer, final double yVer) {
        // testing if node is between requested position and original position
        final Position nodePos = getPosition(node);
        final Position nodeOrientationFromOrigin = new Continuous2DEuclidean(nodePos.getCoordinate(0) - origin.getCoordinate(0), 
                nodePos.getCoordinate(1) - origin.getCoordinate(1));
        final double scalarProductResult1 = xVer * nodeOrientationFromOrigin.getCoordinate(0) + yVer * nodeOrientationFromOrigin.getCoordinate(1);
        // testing if node is near enough to requested position to be an obstacle
        final Position oppositeVersor = new Continuous2DEuclidean(-xVer, -yVer);
        final Position nodeOrientationFromReq = new Continuous2DEuclidean(nodePos.getCoordinate(0) - requestedPos.getCoordinate(0), 
                nodePos.getCoordinate(1) - requestedPos.getCoordinate(1));
        final double scalarProductResult2 = oppositeVersor.getCoordinate(0) * nodeOrientationFromReq.getCoordinate(0) + oppositeVersor.getCoordinate(1) * nodeOrientationFromReq.getCoordinate(1);
        if (scalarProductResult2 <= 0) {
            return (nodePos.getDistanceTo(requestedPos) < node.getRadius() + nodeToMove.getRadius())
                    && scalarProductResult1 >= 0;
        }
        return scalarProductResult1 >= 0;
    }

    // returns the Optional containing the position of the node, if it's an obstacle for movement
    private Optional<Position> getPositionIfNodeIsObstacle(final CellWithCircularArea nodeToMove, 
            final CellWithCircularArea node, 
            final Position originalPos, 
            final double yo,
            final double xo,
            final double yr,
            final double xr) {
        // coordinates of original position, requested position and of node's position
        final double yn = getPosition(node).getCoordinate(1);
        final double xn = getPosition(node).getCoordinate(0);
        // cellular range
        final double cellRange = node.getRadius() + nodeToMove.getRadius();
        // compute intersection
        final double xIntersect;
        final double yIntersect;
        /*
         * yo and yr are converted to float precision to avoid numbers too big or too little.
         * Same for xo and xr.
         */
        if ((float) yo == (float) yr) {
            yIntersect = yo;
            xIntersect = xn;
        } else if ((float) xo == (float) xr) {
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
        final Position intersection = new Continuous2DEuclidean(xIntersect, yIntersect);
        // computes distance between the cell and the first straight line
        final double cat = intersection.getDistanceTo(getPosition(node));
        // if cat is bigger than cellRange, actual cell isn't an obstacle for the cellular movement
        if (cat >= cellRange) {
            // so returns an empty optional
            return Optional.absent();
        }
        // otherwise, compute the maximum practicable distance for the cell
        final double module =  FastMath.sqrt(FastMath.pow((yIntersect - yo), 2) + FastMath.pow((xIntersect - xo), 2));
        if (module == 0) { // if module == 0 the translation is 0, so return originalPos
            return Optional.of(originalPos);
        }
        // compute the versor relative to requested direction of cell movement
        final double cat2 = FastMath.sqrt((FastMath.pow(cellRange, 2) - FastMath.pow(cat, 2)));
        final double distToSum  = originalPos.getDistanceTo(intersection) - cat2;
        final Position versor = new Continuous2DEuclidean((xIntersect - xo) / module, (yIntersect - yo) / module);
        // computes vector representing the practicable movement
        final Position vectorToSum = new Continuous2DEuclidean(distToSum * (versor.getCoordinate(0)), distToSum * (versor.getCoordinate(1)));
        // returns the right position of the cell
        final Position result = originalPos.sum(vectorToSum);
        return Optional.of(result);
    }

    @Override
    protected void nodeAdded(final Node<Double> node, final Position position, final Neighborhood<Double> neighborhood) {
        super.nodeAdded(node, position, neighborhood);
        if (node instanceof CellWithCircularArea) {
            final CellWithCircularArea cell = (CellWithCircularArea) node;
            if (cell.getDiameter() > getMaxDiameterAmongCells()) {
                biggestCell = Optional.of(cell); 
            }
        }
        if (node instanceof CircularDeformableCell) {
            final CircularDeformableCell cell = (CircularDeformableCell) node;
            if (cell.getMaxDiameter() > getMaxDiameterAmongDeformableCells()) {
                biggestDeformableCell = Optional.of(cell); 
            }
        }
    }

    @Override
    protected void nodeRemoved(final Node<Double> node, final Neighborhood<Double> neighborhood) {
        if (biggestCell.isPresent() && biggestCell.get().equals(node)) {
            biggestCell = getNodes().stream()
                    .parallel()
                    .flatMap(n -> n instanceof CellWithCircularArea ? Stream.of((CellWithCircularArea) n) : Stream.empty())
                    .max((c1, c2) -> Double.compare(c1.getDiameter(), c2.getDiameter()))
                    .map(Optional::of)
                    .orElse(Optional.absent());
        }
        if (biggestDeformableCell.isPresent() && biggestDeformableCell.get().equals(node)) {
            biggestDeformableCell = getNodes().stream()
                    .parallel()
                    .flatMap(n -> n instanceof CircularDeformableCell ? Stream.of((CircularDeformableCell) n) : Stream.empty())
                    .max((c1, c2) -> Double.compare(c1.getMaxDiameter(), c2.getMaxDiameter()))
                    .map(Optional::of)
                    .orElse(Optional.absent());
        }
    }

    private double getMaxDiameterAmongCells() {
        return biggestCell
                .transform(n -> n.getDiameter())
                .or(0d);
    }
    @Override
    public double getMaxDiameterAmongDeformableCells() {
        return biggestDeformableCell
                .transform(n -> n.getMaxDiameter())
                .or(0d);
    }

}
