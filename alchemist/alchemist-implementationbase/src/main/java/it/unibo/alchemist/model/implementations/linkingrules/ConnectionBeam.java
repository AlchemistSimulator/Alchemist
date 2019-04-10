/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import static java.lang.Double.NEGATIVE_INFINITY;
import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.nextAfter;
import static org.apache.commons.math3.util.FastMath.nextUp;
import static org.apache.commons.math3.util.FastMath.sin;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;

/**
 * Connects two nodes if, throwing a beam from one to the other, there exists at
 * least one path entirely inside the beam that connects the two nodes. This
 * rule is ideal for environments with obstacles, where the user wants some
 * tolerance in connection breaking.
 * 
 * @param <T>
 * @param <P>
 */
public final class ConnectionBeam<T, P extends Position2D<P>> extends ConnectWithinDistance<T, P> {

    private static final long serialVersionUID = 1L;
    private static final int COORDS = 6;
    private final double beamWidth;
    private transient Environment2DWithObstacles<?, T, P> oenv;
    private transient Area obstacles = new Area();

    /**
     * @param radius
     *            beam maximum length
     * @param beamSize
     *            beam span (tolerance)
     */
    public ConnectionBeam(final double radius, final double beamSize) {
        super(radius);
        beamWidth = beamSize;
    }

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> env) {
        final Neighborhood<T> normal = super.computeNeighborhood(center, env);
        if (oenv == null) {
            if (!(env instanceof Environment2DWithObstacles<?, ?, ?>)) {
                return normal;
            }
            oenv = (Environment2DWithObstacles<?, T, P>) env;
            obstacles.reset();
            oenv.getObstacles().forEach((obs) -> {
                /*
                 * Doubles are prone to approximation errors. Use nextAfter to get rid of them
                 */
                final Rectangle2D bounds = obs.getBounds2D();
                final double mx = nextAfter(bounds.getMinX(), NEGATIVE_INFINITY);
                final double my = nextAfter(bounds.getMinY(), NEGATIVE_INFINITY);
                final double ex = nextUp(bounds.getMaxX());
                final double ey = nextUp(bounds.getMaxY());
                obstacles.add(new Area(new Rectangle2D.Double(mx, my, ex - mx, ey - my)));
            });
        }
        if (!normal.isEmpty()) {
            final P cp = env.getPosition(center);
            final List<Node<T>> neighs = normal.getNeighbors().stream()
                .filter((neigh) -> {
                    final P np = env.getPosition(neigh);
                    return !oenv.intersectsObstacle(cp, np) || projectedBeamOvercomesObstacle(cp, np);
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            return Neighborhoods.make(env, center, neighs);
        }
        return normal;
    }

    private boolean projectedBeamOvercomesObstacle(final P pos1, final P pos2) {
        final double p1x = pos1.getX();
        final double p1y = pos1.getY();
        final double p2x = pos2.getX();
        final double p2y = pos2.getY();
        final double x = p2x - p1x;
        final double y = p2y - p1y;
        /*
         * Compute the angle
         */
        final double angle = atan2(y, x);
        /*
         * Deduce surrounding beam vertices
         */
        final double dx = beamWidth * cos(PI / 2 + angle);
        final double dy = beamWidth * sin(PI / 2 + angle);
        /*
         * Enlarge the beam
         */
        final double cx = beamWidth * cos(angle);
        final double cy = beamWidth * sin(angle);
        /*
         * Create the beam
         */
        final Path2D.Double beamShape = new Path2D.Double();
        beamShape.moveTo(p1x + dx - cx, p1y + dy - cy);
        beamShape.lineTo(p1x - dx - cx, p1y - dy - cy);
        beamShape.lineTo(p2x - dx + cx, p2y - dy + cy);
        beamShape.lineTo(p2x + dx + cx, p2y + dy + cy);
        beamShape.closePath();
        final Area beam = new Area(beamShape);
        /*
         * Perform subtraction
         */
        beam.subtract(obstacles);
        /*
         * Rebuild single areas
         */
        final List<Path2D.Double> subareas = new ArrayList<>();
        Path2D.Double curpath = new Path2D.Double();
        final PathIterator pi = beam.getPathIterator(null);
        final double[] coords = new double[COORDS];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO :
                curpath = new Path2D.Double();
                curpath.moveTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO :
                curpath.lineTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_CLOSE :
                curpath.closePath();
                subareas.add(curpath);
                break;
            default : throw new IllegalArgumentException();
            }
            pi.next();
        }
        /*
         * At least one area must contain both points
         */
        for (final Path2D.Double p : subareas) {
            if (p.contains(p1x, p1y) && p.contains(p2x, p2y)) {
                return true;
            }
        }
        return false;
    }

    private void readObject(final ObjectInputStream o) throws ClassNotFoundException, IOException {
        o.defaultReadObject();
        oenv = null;
        obstacles = new Area();
    }

}
