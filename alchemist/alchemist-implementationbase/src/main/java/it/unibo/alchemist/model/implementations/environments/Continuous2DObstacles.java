/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.github.davidmoten.rtree.Entry;
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithObstacles;
import org.apache.commons.math3.util.Pair;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.internal.EntryDefault;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.utils.RectObstacle2D;
import org.jetbrains.annotations.NotNull;

/**
 * @param <T>
 */
public class Continuous2DObstacles<T> extends LimitedContinuos2D<T> implements EuclideanPhysics2DEnvironmentWithObstacles<RectObstacle2D, T> {

    private static final double TOLERANCE_MULTIPLIER = 0.01;
    private static final long serialVersionUID = 69931743897405107L;
    private transient RTree<RectObstacle2D, Rectangle> rtree = RTree.create();

    @Override
    public final void addObstacle(@NotNull final RectObstacle2D o) {
        rtree = rtree.add(o, toGeometry(o));
        includeObject(o.getMinX(), o.getMaxX(), o.getMinY(), o.getMaxY());
    }

    @NotNull
    @Override
    public final List<RectObstacle2D> getObstacles() {
        return rtree.entries().map(Entry::value).toList().toBlocking().single();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public List<RectObstacle2D> getObstaclesInRange(@NotNull final Euclidean2DPosition center, final double range) {
        return rtree.search(Geometries.circle(center.getX(), center.getY(), range)).map(Entry::value).toList().toBlocking().single();
    }

    /**
     * Subclasses dealing with mobile obstacles may change this.
     *
     * @return false
     */
    @Override
    public boolean hasMobileObstacles() {
        return false;
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public final boolean intersectsObstacle(final Euclidean2DPosition start, final Euclidean2DPosition end) {
        final double sx = start.getX();
        final double sy = start.getY();
        final double ex = end.getX();
        final double ey = end.getY();
        for (final RectObstacle2D obstacle : query(sx, sy, ex, ey, 0)) {
            final double[] coords = obstacle.nearestIntersection(start, end).getCoordinates();
            if (coords[0] != ex || coords[1] != ey || obstacle.contains(coords[0], coords[1])) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected final boolean isAllowed(final Euclidean2DPosition p) {
        return rtree.search(Geometries.point(p.getX(), p.getY())).isEmpty().toBlocking().single();
    }

    @NotNull
    @Override
    public final Euclidean2DPosition next(@NotNull final Euclidean2DPosition current, @NotNull final Euclidean2DPosition desired) {
        return next(current.getX(), current.getY(), desired.getX(), desired.getY());
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public final Euclidean2DPosition next(final double ox, final double oy, final double nx, final double ny) {
        final List<RectObstacle2D> l = query(ox, oy, nx, ny, TOLERANCE_MULTIPLIER);
        if (l.isEmpty()) {
            return new Euclidean2DPosition(nx, ny);
        }
        Pair<Double, Double> shortest = null;
        double fx = nx;
        double fy = ny;
        double fxCache = Double.NaN;
        double fyCache = Double.NaN;
        while (fx != fxCache || fy != fyCache) {
            fxCache = fx;
            fyCache = fy;
            for (int i = 0; i < l.size(); i++) {
                shortest = asPair(l.get(i).next(new Euclidean2DPosition(ox, oy), new Euclidean2DPosition(fx, fy)));
                /*
                 * If one of the dimensions is limited, such limit must be
                 * retained!
                 */
                final double sfx = shortest.getFirst();
                final double sfy = shortest.getSecond();
                if (sfx != fx || fy != sfy) {
                    /*
                     * This obstacle has contributed already
                     */
                    fx = sfx;
                    fy = sfy;
                    l.remove(i);
                    i--;
                }
            }
        }
        return makePosition(shortest.getFirst(), shortest.getSecond());
    }

    private static Pair<Double, Double> asPair(final Euclidean2DPosition coords) {
        return new Pair<>(coords.getX(), coords.getY());
    }

    private List<RectObstacle2D> query(final double ox, final double oy, final double nx, final double ny, final double tolerance) {
        double minx = Math.min(ox, nx);
        double miny = Math.min(oy, ny);
        double maxx = Math.max(ox, nx);
        double maxy = Math.max(oy, ny);
        final double dx = (maxx - minx) * tolerance;
        final double dy = (maxy - miny) * tolerance;
        minx -= dx;
        maxx += dx;
        miny -= dy;
        maxy += dy;
        return rtree.search(Geometries.rectangle(minx, miny, maxx, maxy)).map(Entry::value).toList().toBlocking().single();
    }

    @Override
    public final boolean removeObstacle(@NotNull final RectObstacle2D o) {
        final int initialSize = rtree.size();
        rtree = rtree.delete(o, toGeometry(o));
        return rtree.size() == initialSize - 1;
    }

    private static Rectangle toGeometry(final RectObstacle2D o) {
        return Geometries.rectangle(o.getMinX(), o.getMinY(), o.getMaxX(), o.getMaxY());
    }

    private void writeObject(final ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(getObstacles());
    }

    private void readObject(final ObjectInputStream o) throws ClassNotFoundException, IOException {
        o.defaultReadObject();
        rtree = RTree.create();
        rtree = RTree.<RectObstacle2D, Rectangle>create().add(
            ((List<?>) o.readObject()).parallelStream()
                .map(obs -> (RectObstacle2D) obs)
                .map(obs -> new EntryDefault<>(obs, toGeometry(obs)))
                .collect(Collectors.toList())
        );
    }

}
