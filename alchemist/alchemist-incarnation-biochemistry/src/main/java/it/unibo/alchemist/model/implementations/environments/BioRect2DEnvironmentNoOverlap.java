package it.unibo.alchemist.model.implementations.environments;

import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.math3.util.FastMath;
import it.unibo.alchemist.model.implementations.nodes.CellNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Implements a limited environment supporting cells with a defined shape, 
 * avoiding any overlapping among them.
 * 
 * 
 */
public class BioRect2DEnvironmentNoOverlap extends BioRect2DEnvironment {

    private static final long serialVersionUID = 1L;
    private static final String NOT_SUPPORTED = " is not compatible with biochemistry.";
    private double maxDiameterAmongAllCells;
    private int biggestCellID;

    /**
     * Returns an infinite BioRect2DEnviroment.
     */
    public BioRect2DEnvironmentNoOverlap() {
        super();
        this.maxDiameterAmongAllCells = 0;
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
        this.maxDiameterAmongAllCells = 0;
    }

    @Override
    protected boolean nodeShouldBeAdded(final Node<Double> node, final Position p) {
        if (!(node instanceof CellWithCircularArea)) {
            throw new IllegalArgumentException(node.getClass().getName() + NOT_SUPPORTED);
        }
        final boolean isWithinLimits = super.nodeShouldBeAdded(node, p);
        double range = getMaxDiameterAmongCells();
        if (((CellWithCircularArea) node).getDiameter() > range) {
            range = ((CellWithCircularArea) node).getDiameter();
        }
        final double nodeRadius = ((CellWithCircularArea) node).getRadius();
        return isWithinLimits 
                && !(getNodesWithinRange(p, range).stream()
                        .parallel()
                        .filter(n -> getPosition(n).getDistanceTo(p) < nodeRadius + ((CellWithCircularArea) n).getRadius())
                        .findFirst()
                        .isPresent());
    }

    @Override
    public void moveNodeToPosition(final Node<Double> node, final Position newPos) {
        final double[] cur = getPosition(node).getCartesianCoordinates();
        final double[] np = newPos.getCartesianCoordinates();
        final Position nextWithinLimts = super.next(cur[0], cur[1], np[0], np[1]);
        final Position nextPos = findNearestFreePosition(node, new Continuous2DEuclidean(cur[0], cur[1]), nextWithinLimts);
        super.moveNodeToPosition(node, nextPos);
    }

    /*
     *  finds the first position, in requested direction (requestedPos - originalPos), that can be occupied by the cell.
     */
    private Position findNearestFreePosition(final Node<Double> nodeToMove, final Position originalPos, final Position requestedPos) {
        // get the maximum range depending by cellular shape
        final double maxDiameter = getMaxDiameterAmongCells();
        if (maxDiameter == 0d) {
            return requestedPos;
        }
        final double distanceToReq = originalPos.getDistanceTo(requestedPos);
        final double halfDistance = (distanceToReq / 2);
        // compute position of the midpoint between originalPos and requestedPos
        final double rx = requestedPos.getCoordinate(0);
        final double ox = originalPos.getCoordinate(0);
        final double xMid = (rx - ox) / 2 + ox;
        final double ry = requestedPos.getCoordinate(1);
        final double oy = originalPos.getCoordinate(1);
        final double yMid = (ry - oy) / 2 + oy;
        final Position midPoint = new Continuous2DEuclidean(xMid, yMid);
        // compute optimum scanning range
        double range = FastMath.sqrt(FastMath.pow(halfDistance, 2) + FastMath.pow(maxDiameter, 2));
        final double newMaxDiameter = getNodesWithinRange(midPoint, range).stream()
                .parallel()
                .filter(n -> n instanceof CellWithCircularArea)
                .mapToDouble(n -> ((CellWithCircularArea) n).getDiameter())
                .max()
                .orElse(0);
        range = FastMath.sqrt(FastMath.pow(halfDistance, 2) + FastMath.pow(newMaxDiameter, 2));
        return getNodesWithinRange(midPoint, range).stream()
                .parallel()
                .filter(n -> !getPosition(n).equals(originalPos))
                .filter(new Predicate<Node<Double>>() {

                    @Override
                    public boolean test(final Node<Double> n) {
                        final double xn = getPosition(n).getCoordinate(0);
                        final double yn = getPosition(n).getCoordinate(1);
                        final double xIntersect;
                        final double yIntersect;
                        if (oy == ry) {
                            yIntersect = oy;
                            xIntersect = xn;
                        } else if (ox == rx) {
                            xIntersect = ox;
                            yIntersect = yn;
                        } else {
                            // computes parameters of the straight line from original position to requested position
                            final double m1 = (oy - ry) / (ox - rx);
                            final double q1 = oy - m1 * ox;
                            // computes parameter of straight line, perpendicular to the previous, passing through the cell
                            final double m2 = -1 / m1;
                            final double q2 = yn - m2 * xn;
                            // compute intersection between this two straight lines
                            xIntersect = (q2 - q1) / (m1 - m2);
                            yIntersect = m2 * xIntersect + q2;
                        }
                        final Position intersection = new Continuous2DEuclidean(xIntersect, yIntersect);
                        final double distanceReqInt = intersection.getDistanceTo(requestedPos);
                        final double distanceOrigInt = intersection.getDistanceTo(originalPos);
                        return distanceReqInt < distanceToReq && distanceOrigInt < distanceToReq;
                    }
                }) 
                .map(n -> getPositionIfNodeIsObstacle(nodeToMove, n, originalPos, requestedPos)) 
                .filter(Optional::isPresent) 
                .map(Optional::get)
                .min((p1, p2) -> (int) FastMath.round(p1.getDistanceTo(originalPos) - p2.getDistanceTo(originalPos)))
                .orElse(requestedPos);
    }

    // returns the Optional containing the position of the node, if it's an obstacle for movement
    private Optional<Position> getPositionIfNodeIsObstacle(final Node<Double> nodeToMove, final Node<Double> node, final Position originalPos, final Position requestedPos) {
        if (!(node instanceof CellWithCircularArea) && !(nodeToMove instanceof CellWithCircularArea)) {
            throw new IllegalArgumentException(node.getClass().getName() + NOT_SUPPORTED);
        }
        // coordinates of original position, requested position and of node's position
        final double yo = originalPos.getCoordinate(1);
        final double yr = requestedPos.getCoordinate(1);
        final double xo = originalPos.getCoordinate(0);
        final double xr = requestedPos.getCoordinate(0);
        final double yn = getPosition(node).getCoordinate(1);
        final double xn = getPosition(node).getCoordinate(0);
        // cellular range
        final double cellRange = ((CellWithCircularArea) node).getRadius() + ((CellWithCircularArea) nodeToMove).getRadius();
        // compute intersection
        final double xIntersect;
        final double yIntersect;
        if (yo == yr) {
            yIntersect = yo;
            xIntersect = xn;
        } else if (xo == xr) {
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
            return Optional.empty();
        }
        // otherwise, compute the maximum practicable distance for the cell
        final double cat2 = FastMath.sqrt((FastMath.pow(cellRange, 2) - FastMath.pow(cat, 2)));
        final double distToSum  = originalPos.getDistanceTo(intersection) - cat2;
        // compute the versor relative to requested direction of cell movement
        final double module =  FastMath.sqrt(FastMath.pow((yIntersect - yo), 2) + FastMath.pow((xIntersect - xo), 2));
        final Position versor = new Continuous2DEuclidean((xIntersect - xo) / module, (yIntersect - yo) / module);
        // computes vector representing the practicable movement
        final Position vectorToSum = new Continuous2DEuclidean(distToSum * (versor.getCoordinate(0)), distToSum * (versor.getCoordinate(1)));
        // returns the right position of the cell
        return Optional.of(originalPos.sum(vectorToSum));
    }

    @Override
    protected void nodeAdded(final Node<Double> node, final Position position, final Neighborhood<Double> neighborhood) {
        super.nodeAdded(node, position, neighborhood);
        if (!(node instanceof CellWithCircularArea)) {
            throw new IllegalArgumentException(node.getClass().getName() + NOT_SUPPORTED);
        }
        if (((CellWithCircularArea) node).getDiameter() > this.maxDiameterAmongAllCells) {
            setBiggestCell((CellWithCircularArea) node); 
        }
    }

    @Override
    protected void nodeRemoved(final Node<Double> node, final Neighborhood<Double> neighborhood) {
        if (!(node instanceof CellWithCircularArea)) {
            throw new IllegalArgumentException(node.getClass().getName() + NOT_SUPPORTED);
        }
        if (node.getId() == this.biggestCellID) {
            final CellWithCircularArea newBiggest = (CellWithCircularArea) getNodes().stream()
                    .parallel()
                    .filter(n -> n instanceof CellWithCircularArea)
                    .max((c1, c2) -> (int) (((CellWithCircularArea) c1).getDiameter() - ((CellWithCircularArea) c2).getDiameter()))
                    .orElse(new CellNode(this, 0));
            setBiggestCell(newBiggest);
        }
    }

    private double getMaxDiameterAmongCells() {
        return this.maxDiameterAmongAllCells;
    }

    private void setBiggestCell(final CellWithCircularArea newBiggest) {
        this.biggestCellID = newBiggest.getId();
        this.maxDiameterAmongAllCells = newBiggest.getDiameter();
    }

}
