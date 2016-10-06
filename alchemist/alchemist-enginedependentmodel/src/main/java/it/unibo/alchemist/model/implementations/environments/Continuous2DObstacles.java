/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.internal.EntryDefault;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.utils.RectObstacle2D;
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * @param <T>
 */
public class Continuous2DObstacles<T> extends LimitedContinuos2D<T> implements Environment2DWithObstacles<RectObstacle2D, T> {

    private static final double TOLERANCE_MULTIPLIER = 0.01;
    /**
     * Default maximum communication range.
     */
    public static final double DEFAULT_MAX_RANGE = 1.5;
    private static final long serialVersionUID = 69931743897405107L;
    private transient RTree<RectObstacle2D, Rectangle> rtree = RTree.create();

    @Override
    public void addObstacle(final RectObstacle2D o) {
        rtree = rtree.add(o, toGeometry(o));
        includeObject(o.getMinX(), o.getMaxX(), o.getMinY(), o.getMaxY());
    }

    @Override
    public List<RectObstacle2D> getObstacles() {
        return rtree.entries().map(e -> e.value()).toList().toBlocking().single();
    }

    @Override
    public List<RectObstacle2D> getObstaclesInRange(final Double centerx, final Double centery, final Double range) {
        return rtree.search(Geometries.circle(centerx, centery, range)).map(e -> e.value()).toList().toBlocking().single();
    }

    @Override
    public boolean hasMobileObstacles() {
        return false;
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public boolean intersectsObstacle(final double sx, final double sy, final double ex, final double ey) {
        for (final RectObstacle2D obstacle : query(sx, sy, ex, ey, 0)) {
            final double[] coords = obstacle.nearestIntersection(sx, sy, ex, ey);
            if (coords[0] != ex || coords[1] != ey || obstacle.contains(coords[0], coords[1])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean intersectsObstacle(final Position p1, final Position p2) {
        return intersectsObstacle(p1.getCoordinate(0), p1.getCoordinate(1), p2.getCoordinate(0), p2.getCoordinate(1));
    }

    @Override
    protected boolean isAllowed(final Position p) {
        return rtree.search(Geometries.point(p.getCoordinate(0), p.getCoordinate(1))).isEmpty().toBlocking().single();
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public final Position next(final double ox, final double oy, final double nx, final double ny) {
        final List<RectObstacle2D> l = query(ox, oy, nx, ny, TOLERANCE_MULTIPLIER);
        if (l.isEmpty()) {
            return new Continuous2DEuclidean(nx, ny);
        }
        Position shortest = null;
        double fx = nx;
        double fy = ny;
        double fxCache = Double.NaN;
        double fyCache = Double.NaN;
        while (fx != fxCache || fy != fyCache) {
            fxCache = fx;
            fyCache = fy;
            for (int i = 0; i < l.size(); i++) {
                shortest = l.get(i).next(ox, oy, fx, fy);
                /*
                 * If one of the dimensions is limited, such limit must be
                 * retained!
                 */
                final double sfx = shortest.getCoordinate(0);
                final double sfy = shortest.getCoordinate(1);
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
        return shortest;
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
        return rtree.search(Geometries.rectangle(minx, miny, maxx, maxy)).map(e -> e.value()).toList().toBlocking().single();
    }

    @Override
    public boolean removeObstacle(final RectObstacle2D o) {
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
