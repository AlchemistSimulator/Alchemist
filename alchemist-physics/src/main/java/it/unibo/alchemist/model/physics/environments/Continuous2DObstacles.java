/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.physics.environments;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.internal.EntryDefault;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.euclidean.obstacles.RectObstacle2D;
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Incarnation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <T> concentration type
 */
public class Continuous2DObstacles<T> extends LimitedContinuos2D<T>
    implements EuclideanPhysics2DEnvironmentWithObstacles<RectObstacle2D<Euclidean2DPosition>, T> {

    private static final double TOLERANCE_MULTIPLIER = 0.01;
    private static final long serialVersionUID = 69931743897405107L;
    private transient RTree<RectObstacle2D<Euclidean2DPosition>, Rectangle> rtree = RTree.create();

    /**
     * @param incarnation the current incarnation.
     */
    public Continuous2DObstacles(final Incarnation<T, Euclidean2DPosition> incarnation) {
        super(incarnation);
    }

    @Override
    public final void addObstacle(@Nonnull final RectObstacle2D<Euclidean2DPosition> o) {
        rtree = rtree.add(o, toGeometry(o));
        includeObject(o.getMinX(), o.getMaxX(), o.getMinY(), o.getMaxY());
    }

    @Nonnull
    @Override
    public final List<RectObstacle2D<Euclidean2DPosition>> getObstacles() {
        return rtree.entries().map(Entry::value).toList().toBlocking().single();
    }

    @Override
    @Nonnull
    public final List<RectObstacle2D<Euclidean2DPosition>> getObstaclesInRange(
            @Nonnull final Euclidean2DPosition center,
            final double range
    ) {
        return getObstaclesInRange(center.getX(), center.getY(), range);
    }

    @Nonnull
    @Override
    public final List<RectObstacle2D<Euclidean2DPosition>> getObstaclesInRange(
            final double centerx,
            final double centery,
            final double range
    ) {
        return rtree.search(Geometries.circle(centerx, centery, range)).map(Entry::value).toList().toBlocking().single();
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
        for (final RectObstacle2D<Euclidean2DPosition> obstacle : query(sx, sy, ex, ey, 0)) {
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

    @Nonnull
    @Override
    public final Euclidean2DPosition next(
            @Nonnull final Euclidean2DPosition current,
            @Nonnull final Euclidean2DPosition desired
    ) {
        return next(current.getX(), current.getY(), desired.getX(), desired.getY());
    }

    @Override
    @SuppressFBWarnings({ "FE_FLOATING_POINT_EQUALITY", "FL_FLOATS_AS_LOOP_COUNTERS" })
    public final Euclidean2DPosition next(final double ox, final double oy, final double nx, final double ny) {
        final List<RectObstacle2D<Euclidean2DPosition>> l = query(ox, oy, nx, ny, TOLERANCE_MULTIPLIER);
        if (l.isEmpty()) {
            return new Euclidean2DPosition(nx, ny);
        }
        Euclidean2DPosition shortest = null;
        double fx = nx;
        double fy = ny;
        double fxCache = Double.NaN;
        double fyCache = Double.NaN;
        while (fx != fxCache || fy != fyCache) {
            fxCache = fx;
            fyCache = fy;
            for (int i = 0; i < l.size(); i++) {
                shortest = l.get(i).next(new Euclidean2DPosition(ox, oy), new Euclidean2DPosition(fx, fy));
                /*
                 * If one of the dimensions is limited, such limit must be
                 * retained!
                 */
                final double sfx = shortest.getX();
                final double sfy = shortest.getY();
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
        return makePosition(shortest.getX(), shortest.getY());
    }

    private List<RectObstacle2D<Euclidean2DPosition>> query(
            final double ox,
            final double oy,
            final double nx,
            final double ny,
            final double tolerance
    ) {
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
        return rtree.search(Geometries.rectangle(minx, miny, maxx, maxy))
                .map(Entry::value)
                .toList()
                .toBlocking()
                .single();
    }

    @Override
    public final boolean removeObstacle(@Nonnull final RectObstacle2D<Euclidean2DPosition> o) {
        final int initialSize = rtree.size();
        rtree = rtree.delete(o, toGeometry(o));
        return rtree.size() == initialSize - 1;
    }

    private static Rectangle toGeometry(final RectObstacle2D<Euclidean2DPosition> o) {
        return Geometries.rectangle(o.getMinX(), o.getMinY(), o.getMaxX(), o.getMaxY());
    }

    private void writeObject(final ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(getObstacles());
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream o) throws ClassNotFoundException, IOException {
        o.defaultReadObject();
        rtree = RTree.create();
        rtree = RTree.<RectObstacle2D<Euclidean2DPosition>, Rectangle>create().add(
            ((List<?>) o.readObject()).parallelStream()
                .map(obs -> (RectObstacle2D<Euclidean2DPosition>) obs)
                .map(obs -> new EntryDefault<>(obs, toGeometry(obs)))
                .collect(Collectors.toList())
        );
    }

}
