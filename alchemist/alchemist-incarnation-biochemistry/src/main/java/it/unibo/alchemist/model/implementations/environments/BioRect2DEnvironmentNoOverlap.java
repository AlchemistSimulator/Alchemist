package it.unibo.alchemist.model.implementations.environments;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellShape;
import it.unibo.alchemist.model.interfaces.ICellNodeWithShape;
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
        final double range = ((ICellNodeWithShape) node).getShape().getMaxRange();
        return isWithinLimits 
                && !(getNodesWithinRange(p, range).stream()
                     .filter(n -> getPosition(n).getDistanceTo(p) < ((ICellNodeWithShape) node).getShape().getMaxRange())
                     .findFirst()
                     .isPresent());
    };

    @Override
    protected Position next(final double ox, final double oy, final double nx, final double ny) {
        // check if the requested position (nx, ny) is within limits
        Position nextPos = super.next(ox, oy, nx, ny);
        // check if the requested position is already occupied by some other cells
        nextPos = findNearestFreePosition(new Continuous2DEuclidean(ox, oy), nextPos);
        return nextPos;
    }

    // finds the first position, in requested direction (requestedPos - originalPos), that can be occupied by the cell.
    private Position findNearestFreePosition(final Position originalPos, final Position requestedPos) {
        // get the maximum range depending by cellular shape
        final double cellRange = getNodes().stream()
                .mapToDouble(e -> ((ICellNodeWithShape) e).getShape().getMaxRange())
                .findAny()
                .orElseGet(() -> 0d);
        if (cellRange == 0d) {
            return requestedPos;
        }
        final double halfDistance = (originalPos.getDistanceTo(requestedPos) / 2);
        final double range = FastMath.sqrt(FastMath.pow(halfDistance, 2) + FastMath.pow(cellRange, 2));
        // compute position of the midpoint between originalPos and requestedPos
        final Position midPoint = new Continuous2DEuclidean(((requestedPos.getCoordinate(0) - originalPos.getCoordinate(0)) / 2) + originalPos.getCoordinate(0), ((requestedPos.getCoordinate(1) - originalPos.getCoordinate(1)) / 2) + originalPos.getCoordinate(1));
        return getNodesWithinRange(midPoint, range).stream()
                .parallel()
                .filter(n -> !getPosition(n).equals(originalPos))
                .map(n -> getPositionIfNodeIsObstacle(n, originalPos, requestedPos)) 
                .filter(Optional::isPresent) 
                .map(Optional::get)
                .min((p1, p2) -> (int) FastMath.round(p1.getDistanceTo(originalPos) - p2.getDistanceTo(originalPos)))
                .orElse(requestedPos);
    }

    // returns the Optional containing the position of the node, if it's an obstacle for movement
    private Optional<Position> getPositionIfNodeIsObstacle(final Node<Double> node, final Position originalPos, final Position requestedPos) {
        // coordinates of original position, requested position and of node's position
        final double yo = originalPos.getCoordinate(1);
        final double yr = requestedPos.getCoordinate(1);
        final double xo = originalPos.getCoordinate(0);
        final double xr = requestedPos.getCoordinate(0);
        final double yn = getPosition(node).getCoordinate(1);
        final double xn = getPosition(node).getCoordinate(0);
        // cellular range
        final double cellRange = ((ICellNodeWithShape) node).getShape().getMaxRange();
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

    /**
     * Given a {@link Collection} of nodes, returns a {@link List} the shapes of the nodes.
     * @param nodes the {@link Collection} of nodes
     * @return a {@link List} of the nodes' shapes.
     */
    public List<CellShape> getCellShapes(final Collection<Node<Double>> nodes) {
        return nodes.stream()
                .map(n -> ((ICellNodeWithShape) n).getShape())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 
     * @return a {@link List} containing all cell shapes in this environment.
     */
    public List<CellShape> getAllCellShapes() {
        return getCellShapes(getNodes());
    }

    private double getMaximumRangeAmongCellShapes(final Collection<CellShape> shapes) {
        return shapes.stream()
                .map(s -> s.getMaxRange())
                .max((r1, r2) -> (int) FastMath.round(r1 - r2))
                .orElseGet(() -> 0d);
    }

    private double getMaximumRangeAmongAllCellShapes() {
        return getMaximumRangeAmongCellShapes(getAllCellShapes());
    }

    private double getMinimumRangeAmongCellShapes(final Collection<CellShape> shapes) {
        return shapes.stream()
                .map(s -> s.getMaxRange())
                .min((r1, r2) -> (int) FastMath.round(r1 - r2))
                .orElseGet(() -> 0d);
    }

    private double getMinimumRangeAmongAllCellShapes() {
        return getMinimumRangeAmongCellShapes(getAllCellShapes());
    }
}
