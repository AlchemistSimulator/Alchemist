/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.routes;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.danilopianini.util.Hashes;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Route;

/**
 * Abstract route implementation.
 * 
 * @param <P> the type of position that the route is composed
 */
public class PolygonalChain<P extends Position<?>> implements Route<P> {

    private static final long serialVersionUID = 1L;
    private double distance = Double.NaN;
    private int hash;
    private final ImmutableList<P> positions;

    /**
     * @param positions
     *            the positions this route traverses
     */
    @SafeVarargs
    public PolygonalChain(final P... positions) {
        this(ImmutableList.copyOf(positions));
    }

    /**
     * @param positions
     *            the positions this route traverses
     */
    public PolygonalChain(final List<P> positions) {
        if (Objects.requireNonNull(positions).size() == 0) {
            throw new IllegalArgumentException("At least one point is required for creating a Route");
        }
        this.positions = positions instanceof ImmutableList
                ? (ImmutableList<P>) positions
                : ImmutableList.copyOf(positions);
    }

    /**
     * @param p1
     *            first position
     * @param p2
     *            second position
     * @return the distance between p1 and p2
     * @param <U>
     *            upper {@link Position} type, used internally
     */
    @SuppressWarnings("unchecked")
    protected <U extends Position<U>> double computeDistance(final P p1, final P p2) {
        if (p1.getClass() == p2.getClass() || p1.getClass().isAssignableFrom(p2.getClass())) {
            return ((U) p1).getDistanceTo((U) p2);
        } else if (p2.getClass().isAssignableFrom(p1.getClass())) {
            return ((U) p2).getDistanceTo((U) p1);
        }
        throw new UncomparableDistancesException(p1, p2);
    }

    @Override
    public final boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        return other.getClass().equals(getClass()) && positions.equals(((PolygonalChain<?>) other).positions);
    }

    @Override
    public final P getPoint(final int step) {
        if (step < size()) {
            return positions.get(step);
        }
        throw new IllegalArgumentException(step + " is not a valid point number for this route (length " + size() + ')');
    }

    @Override
    public final ImmutableList<P> getPoints() {
        return positions;
    }

    @Override
    public final int hashCode() {
        if (hash == 0) {
            hash = Hashes.hash32(positions);
        }
        return hash;
    }

    @Override
    public final Iterator<P> iterator() {
        return positions.iterator();
    }

    @Override
    public final double length() {
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
    public final int size() {
        return positions.size();
    }

    @Override
    public final Stream<P> stream() {
        return positions.stream();
    }

    /**
     * Prints the class name and the list of positions.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + positions;
    }
}
