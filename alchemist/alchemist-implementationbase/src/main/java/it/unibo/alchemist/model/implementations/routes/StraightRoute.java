package it.unibo.alchemist.model.implementations.routes;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Route;

/**
 * Abstract route implementation.
 */
public class StraightRoute implements Route {

    private static final long serialVersionUID = 1L;
    private final List<Position> positions;
    private double distance = Double.NaN;

    /**
     * @param positions the positions this route traverses
     */
    public StraightRoute(final Position... positions) {
        this.positions = Collections.unmodifiableList(Lists.newArrayList(positions));
    }

    @Override
    public double getDistance() {
        if (Double.isNaN(distance) && getPointsNumber() > 0) {
            distance = 0;
            final Iterator<Position> iter = positions.iterator();
            for (Position cur = iter.next(); iter.hasNext();) {
                final Position next = iter.next();
                distance += computeDistance(cur, next);
                cur = next;
            }
        }
        return distance;
    }

    /**
     * @param p1
     *            first position
     * @param p2
     *            second position
     * @return the distance between p1 and p2
     */
    protected double computeDistance(final Position p1, final Position p2) {
        return p1.getDistanceTo(p2);
    }

    @Override
    public Position getPoint(final int step) {
        if (step < getPointsNumber()) {
            return positions.get(step);
        }
        throw new IllegalArgumentException(step + " is not a valid point number for this route (lenght " + getPointsNumber() + ')');
    }

    @Override
    public List<Position> getPoints() {
        return positions;
    }

    @Override
    public int getPointsNumber() {
        return positions.size();
    }

    @Override
    public double getTime() {
        return 0;
    }
}
