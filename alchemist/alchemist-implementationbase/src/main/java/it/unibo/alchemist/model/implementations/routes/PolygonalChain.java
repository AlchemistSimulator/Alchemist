package it.unibo.alchemist.model.implementations.routes;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import org.danilopianini.util.Hashes;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Route;

/**
 * Abstract route implementation.
 * 
 * @param <P> the type of position that the route is composed
 */
public class PolygonalChain<P extends Position> implements Route<P> {

    private static final long serialVersionUID = 1L;
    private double distance = Double.NaN;
    private int hash;
    private final ImmutableList<P> positions;

    /**
     * @param positions the positions this route traverses
     */
    @SafeVarargs
    public PolygonalChain(final P... positions) {
        if (Objects.requireNonNull(positions).length == 0) {
            throw new IllegalArgumentException("At least one point is required for creating a Route");
        }
        this.positions = ImmutableList.copyOf(positions);
    }

    /**
     * @param p1
     *            first position
     * @param p2
     *            second position
     * @return the distance between p1 and p2
     */
    protected double computeDistance(final P p1, final P p2) {
        return p1.getDistanceTo(p2);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        return other.getClass().equals(getClass()) && positions.equals(((PolygonalChain<?>) other).positions);
    }

    @Override
    public P getPoint(final int step) {
        if (step < size()) {
            return positions.get(step);
        }
        throw new IllegalArgumentException(step + " is not a valid point number for this route (length " + size() + ')');
    }

    @Override
    public ImmutableList<P> getPoints() {
        return positions;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Hashes.hash32(positions);
        }
        return hash;
    }

    @Override
    public Iterator<P> iterator() {
        return positions.iterator();
    }

    @Override
    public double length() {
        if (Double.isNaN(distance) && size() > 0) {
            distance = 0;
            final Iterator<P> iter = positions.iterator();
            for (P cur = iter.next(); iter.hasNext();) {
                final P next = iter.next();
                distance += computeDistance(cur, next);
                cur = next;
            }
        }
        return distance;
    }

    @Override
    public int size() {
        return positions.size();
    }

    @Override
    public Stream<P> stream() {
        return positions.stream();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + positions;
    }
}
