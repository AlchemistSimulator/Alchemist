/**
 * 
 */
package it.unibo.alchemist.model.implementations;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Route;

import java.util.List;
import java.util.Objects;

import org.danilopianini.util.Hashes;

import com.google.common.collect.Lists;

/**
 * Very simple route, the shortest path connecting the two passed points.
 * 
 */
public class PointToPointRoute implements Route {

    /**
     * 
     */
    private static final long serialVersionUID = -6937104566388182150L;
    private final Position s, e;
    private String string;
    private double dist = Double.NaN;
    private List<Position> l; // Optional is not Serializable!

    /**
     * 
     * @param start
     *            start position
     * @param end
     *            end position
     */
    public PointToPointRoute(final Position start, final Position end) {
        s = Objects.requireNonNull(start);
        e = Objects.requireNonNull(end);
    }

    @Override
    public double getDistance() {
        if (Double.isNaN(dist)) {
            dist = s.getDistanceTo(e);
        }
        return dist;
    }

    @Override
    public Position getPoint(final int step) {
        return step <= 0 ? s : e;
    }

    @Override
    public List<Position> getPoints() {
        if (l == null) {
            l = Lists.newArrayList(s, e);
        }
        return l;
    }

    @Override
    public int getPointsNumber() {
        return 2;
    }

    @Override
    public double getTime() {
        return Double.NaN;
    }

    @Override
    public String toString() {
        if (string == null) {
            string = "[" + s + " >> " + e + "]";
        }
        return string;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Route) {
            final Route r = (Route) obj;
            return getPoints().equals(r.getPoints());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Hashes.hash32(getPoints());
    }

}
