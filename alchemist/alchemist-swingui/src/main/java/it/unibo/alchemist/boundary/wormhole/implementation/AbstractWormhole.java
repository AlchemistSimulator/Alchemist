package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

/**
 * Partial, abstract, implementation for the interface {@link IWormhole2D}.
 * <br/>
 * This implementation considers the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 *
 * @param <T> the view component type
 */
public abstract class AbstractWormhole<T> implements IWormhole2D {
    private final Environment<?> environment;
    private final T view;
    private PointAdapter position;
    private double zoom = 1d;
    private double hRate = 1d;
    private double vRate = 1d;
    private double rotation;
    private Mode mode = Mode.ISOMETRIC;
    private PointAdapter effectCenter = from(0, 0);

    /**
     * Default constructor.
     * <br/>
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param environment the {@code Environment}
     * @param view        the controlled view
     * @param position    the position
     */
    public AbstractWormhole(final Environment<?> environment, final T view, final PointAdapter position) {
        this.environment = environment;
        this.view = view;
        this.position = position;
    }

    @Override
    public Position getEnvPoint(final Point viewPoint) {
        return envPointFromView(from(viewPoint)).toPosition();
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public Point getViewPoint(final Position envPoint) {
        return viewPointFromEnv(from(envPoint)).toPoint();
    }

    /**
     * Translates a point to the view space.
     *
     * @param envPoint env space point
     * @return view space point
     */
    protected PointAdapter viewPointFromEnv(final PointAdapter envPoint) {
        final PointAdapter envp = envPoint.diff(effectCenter);
        final Point2D ep = envp.toPoint2D();
        final AffineTransform t = calculateTransform();
        t.transform(ep, ep);
        return from(ep);
    }

    @Override
    public Point getViewPosition() {
        return position.toPoint();
    }

    @Override
    public void setViewPosition(final Point viewPoint) {
        this.position = from(viewPoint);
    }

    @Override
    public abstract Dimension2D getViewSize();

    @Override
    public double getZoom() {
        return this.zoom;
    }

    @Override
    public void setZoom(final double zoom) {
        if (zoom <= 0) {
            this.zoom = 0d;
        }
        this.zoom = zoom;
    }

    @Override
    public boolean isInsideView(final Point viewPoint) {
        return false;
    }

    @Override
    public void rotateAroundPoint(final Point p, final double a) {
        final PointAdapter orig = effectCenter;
        setViewPositionWithoutMoving(from(p));
        setRotation(a);
        setEnvPositionWithoutMoving(orig);
    }

    /**
     * The method changes the point referred ad 'position'.
     *
     * @param orig is a {@link Point2D} into the env-space
     */
    private void setEnvPositionWithoutMoving(final PointAdapter orig) {
        setViewPositionWithoutMoving(viewPointFromEnv(orig));
    }

    /**
     * The method changes the point referred ad 'position'.
     *
     * @param from is a {@link Point2D} into the view-space
     */
    private void setViewPositionWithoutMoving(final PointAdapter from) {
        final PointAdapter envDelta = envPointFromView(from).diff(envPointFromView(position));
        position = from;
        effectCenter = effectCenter.sum(envDelta);
    }

    @Override
    public void setEnvPosition(final Position envPoint) {
        setViewPosition(getViewPoint(envPoint));
    }

    @Override
    public abstract void optimalZoom();

    @Override
    public void zoomOnPoint(final Point point, final double zoomRate) {
        final PointAdapter orig = effectCenter;
        setViewPositionWithoutMoving(from(point));
        setZoom(zoomRate);
        setEnvPositionWithoutMoving(orig);
    }

    @Override
    public void center() {
        final double[] off = getEnvironment().getOffset();
        final double[] size = getEnvironment().getSize();
        final PointAdapter center = Double.isNaN(off[0]) || Double.isNaN(off[1]) || size[0] <= 0 || size[1] <= 0
                ? from(0, 0)
                : from(off[0] + size[0] / 2, off[1] + size[1] / 2);
        setEnvPosition(center.toPosition());
    }

    /**
     * Getter method for the {@code Environment} model.
     *
     * @return the {@code Environment} model
     */
    protected final Environment<?> getEnvironment() {
        return this.environment;
    }

    /**
     * Getter method for the controlled View.
     *
     * @return the controlled View
     */
    protected final T getView() {
        return this.view;
    }

    /**
     * Getter method for position field.
     *
     * @return the position
     */
    protected final PointAdapter getPosition() {
        return position;
    }

    /**
     * Setter method for position field.
     *
     * @param position the position to set
     */
    protected final void setPosition(final PointAdapter position) {
        this.position = position;
    }

    /**
     * Translates a point to the env space.
     *
     * @param viewPoint view space point
     * @return env space point
     */
    protected final PointAdapter envPointFromView(final PointAdapter viewPoint) {
        final Point2D.Double vp = new Point2D.Double(viewPoint.toPoint().x, viewPoint.toPoint().y);
        final AffineTransform t = calculateTransform();
        try {
            t.inverseTransform(vp, vp);
        } catch (final NoninvertibleTransformException e) {
            getLogger().error("Unable to perform the transformation from view point to env point. Please check if this method has been called after making the UI visible", e);
        }
        return from(vp);
    }

    /**
     * Calculates the {@link AffineTransform} that allows the wormhole to
     * convert points from env-space to view-space.
     *
     * @return an {@code AffineTransform} object
     */
    protected AffineTransform calculateTransform() {
        final AffineTransform t;
        if (getMode() == Mode.ISOMETRIC) {
            t = new AffineTransform(
                    getZoom(), 0d, 0d,
                    -getZoom(), getViewPosition().getX(), getViewPosition().getY());
        } else {
            t = new AffineTransform(getZoom() * getHRate(), 0d, 0d,
                    -getZoom() * getVRate(), getViewPosition().getX(), getViewPosition().getY());
        }
        t.concatenate(AffineTransform.getRotateInstance(getRotation()));
        return t;
    }

    /**
     * Getter method for the rotation angle, in radians.
     *
     * @return a {@code double} value representing an angle expressed with radians
     * @see #setRotation(double)
     */
    protected double getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(final double rad) {
        this.rotation = rad % (Math.PI * 2d);
    }

    /**
     * Getter method for the horizontal stretch rate.
     *
     * @return a {@code double} value representing the horizontal stretch rate
     */
    protected double getHRate() {
        return this.hRate;
    }

    /**
     * Getter method for the vertical stretch rate.
     *
     * @return a {@code double} value representing the vertical stretch rate
     */
    protected double getVRate() {
        return this.vRate;
    }

    /**
     * Getter method for the {@link Logger} of this class.
     *
     * @return the {@code Logger}
     */
    protected abstract Logger getLogger();

    /**
     * Returns the dimensions ratio of the {@link Environment} used as model.
     *
     * @return the dimensions ratio
     * @see Environment#getSize()
     */
    protected double getEnvRatio() {
        final double[] size = environment.getSize();
        return size[0] / size[1];
    }

    /**
     * Returns the dimensions ratio of the controlled view.
     *
     * @return the dimensions ratio
     */
    protected abstract double getViewRatio();
}
