package it.unibo.alchemist.boundary.wormhole.implementation;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

import org.danilopianini.lang.HashUtils;

import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Adapts various representations of bidimensional positions.
 */
public final class PointAdapter implements Serializable {

    private static final long serialVersionUID = 4144646922749713533L;
    private final double x, y;
    private Position pos;
    private int hash;

    private PointAdapter(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    private PointAdapter(final Position pos) {
        assert pos.getDimensions() == 2;
        this.pos = pos;
        x = pos.getCoordinate(0);
        y = pos.getCoordinate(1);
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
     * @return a {@link PointAdapter}
     */
    public static PointAdapter from(final double x, final double y) {
        return new PointAdapter(x, y);
    }

    /**
     * Builds a {@link PointAdapter}.
     * 
     * @param p
     *            the {@link Position}
     * 
     * @return a {@link PointAdapter}
     */
    public static PointAdapter from(final Position p) {
        return new PointAdapter(p);
    }

    /**
     * Builds a {@link PointAdapter}.
     * 
     * @param p
     *            the {@link Point2D}
     * 
     * @return a {@link PointAdapter}
     */
    public static PointAdapter from(final Point2D p) {
        return new PointAdapter(p.getX(), p.getY());
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
     * @return the {@link Position} view of this {@link PointAdapter}
     */
    public Position toPosition() {
        if (pos == null) {
            pos = new Continuous2DEuclidean(x, y);
        }
        return pos;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof PointAdapter && ((PointAdapter) obj).x == x && ((PointAdapter) obj).y == y;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = HashUtils.hash32(x, y);
        }
        return hash;
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

    /**
     * @param op
     *            the {@link PointAdapter} to sum
     * @return a new {@link PointAdapter} obtained by subtracting the passed
     *         argument to this {@link PointAdapter}
     */
    public PointAdapter diff(final PointAdapter op) {
        return new PointAdapter(x - op.x, y - op.y);
    }

    /**
     * @param op
     *            the {@link PointAdapter} to sum
     * @return a new {@link PointAdapter} obtained by summing the passed
     *         argument to this {@link PointAdapter}
     */
    public PointAdapter sum(final PointAdapter op) {
        return new PointAdapter(x + op.x, y + op.y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

}
