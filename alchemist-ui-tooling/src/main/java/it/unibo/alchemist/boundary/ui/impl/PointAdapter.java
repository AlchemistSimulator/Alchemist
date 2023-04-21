/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.ui.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.Position2D;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Objects;

/**
 * Adapts various representations of bidimensional positions.
 *
 * @param <P> position type
 */
public final class PointAdapter<P extends Position2D<? extends P>> implements Serializable {

    private static final long serialVersionUID = 4144646922749713533L;
    private final double x, y;
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Recomputed in case of necessity")
    private transient int hash;
    private transient P pos;

    private PointAdapter(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    private PointAdapter(final P pos) {
        this.pos = pos;
        x = pos.getX();
        y = pos.getY();
    }

    /**
     * @param op
     *            the {@link PointAdapter} to sum
     * @return a new {@link PointAdapter} obtained by subtracting the passed
     *         argument to this {@link PointAdapter}
     */
    public PointAdapter<P> diff(final PointAdapter<?> op) {
        return new PointAdapter<>(x - op.x, y - op.y);
    }

    @Override
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "Made by purpose")
    public boolean equals(final Object obj) {
        return this == obj
            || obj instanceof PointAdapter && ((PointAdapter<?>) obj).x == x && ((PointAdapter<?>) obj).y == y;
    }

    /**
     * @return x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * @return y coordinate
     */
    public double getY() {
        return y;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(x, y);
        }
        return hash;
    }

    /**
     * @param op
     *            the {@link PointAdapter} to sum
     * @return a new {@link PointAdapter} obtained by summing the passed
     *         argument to this {@link PointAdapter}
     */
    public PointAdapter<P> sum(final PointAdapter<?> op) {
        return new PointAdapter<>(x + op.x, y + op.y);
    }

    /**
     * @return the {@link Point} view of this {@link PointAdapter}
     */
    public Point toPoint() {
        return new Point(approx(x), approx(y));
    }

    /**
     * @return the {@link Point2D} view of this {@link PointAdapter}
     */
    public Point2D toPoint2D() {
        return new Point2D.Double(x, y);
    }

    /**
     * @param environment the environment
     *
     * @return the {@link it.unibo.alchemist.model.Position} view of this {@link PointAdapter}
     */
    public P toPosition(final Environment<?, P> environment) {
        if (pos == null) {
            pos = environment instanceof MapEnvironment
                ? environment.makePosition(getY(), getX())
                : environment.makePosition(getX(), getY());
        }
        return pos;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    private static int approx(final double d) {
        return (int) Math.round(d);
    }

    /**
     * Builds a {@link PointAdapter} from coordinates.
     *
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param <P>
     *            Position type
     *
     * @return a {@link PointAdapter}
     */
    public static <P extends Position2D<? extends P>> PointAdapter<P> from(final double x, final double y) {
        return new PointAdapter<>(x, y);
    }

    /**
     * Builds a {@link PointAdapter}.
     *
     * @param p
     *            the {@link it.unibo.alchemist.model.Position}
     * @param <P>
     *            Position type
     * 
     * @return a {@link PointAdapter}
     */
    @SuppressWarnings("unchecked")
    public static <P extends Position2D<? extends P>> PointAdapter<P> from(final Position2D<?> p) {
        return new PointAdapter<>((P) p);
    }

    /**
     * Builds a {@link PointAdapter}.
     *
     * @param p
     *            the {@link Point2D}
     * @param <P>
     *            Position type
     *
     * @return a {@link PointAdapter}
     */
    public static <P extends Position2D<? extends P>> PointAdapter<P> from(final Point2D p) {
        return new PointAdapter<>(p.getX(), p.getY());
    }

}
