package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.boundary.wormhole.interfaces.ViewType;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import org.slf4j.Logger;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

/**
 * Partial, abstract, implementation for the interface {@link BidimensionalWormhole}.
 * <br/>
 * This implementation considers the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 * <br/>
 * This abstract class is independent from the 2D graphical component wrapped.
 */
public abstract class AbstractWormhole2D<P extends Position2D<? extends P>> implements BidimensionalWormhole<P> {
    private final Environment<?, P> environment;
    private final ViewType view;
    private PointAdapter<P> position;
    private double zoom = 1d;
    private double hRate = 1d;
    private double vRate = 1d;
    private double rotation;
    private Mode mode = Mode.ISOMETRIC;
    private PointAdapter<P> effectCenter = from(0, 0);

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
    public AbstractWormhole2D(final Environment<?, P> environment, final ViewType view, final PointAdapter<P> position) {
        this.environment = environment;
        this.view = view;
        this.position = position;
    }

    @Override
    public P getEnvPoint(final Point viewPoint) {
        return envPointFromView(from(viewPoint)).toPosition(environment);
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    /**
     * Allows child-classes to modify the {@link #mode} field.
     *
     * @param mode is the new {@link #mode}
     */
    protected void setMode(final Mode mode) {
        this.mode = mode;
        if (mode == Mode.ADAPT_TO_VIEW) {
            vRate = getNIVerticalRatio();
            hRate = getNIHorizontalRatio();
        }
    }

    @Override
    public Point getViewPoint(final Position2D<?> envPoint) {
        return viewPointFromEnv(PointAdapter.from(envPoint)).toPoint();
    }

    /**
     * Translates a point to the view space.
     *
     * @param envPoint env space point
     * @return view space point
     */
    protected PointAdapter<P> viewPointFromEnv(final PointAdapter<P> envPoint) {
        final PointAdapter<P> envp = envPoint.diff(effectCenter);
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
        final double x = viewPoint.getX();
        final double y = viewPoint.getY();
        final Dimension2D vs = getViewSize();
        return x >= 0 && x <= vs.getWidth() && y >= 0 && y <= vs.getHeight();
    }

    @Override
    public void rotateAroundPoint(final Point p, final double a) {
        final PointAdapter<P> orig = effectCenter;
        setViewPositionWithoutMoving(from(p));
        setRotation(a);
        setEnvPositionWithoutMoving(orig);
    }

    /**
     * The method changes the point referred ad 'position'.
     *
     * @param envPoint is a {@link Point2D} into the env-space
     */
    private void setEnvPositionWithoutMoving(final PointAdapter<P> envPoint) {
        setViewPositionWithoutMoving(viewPointFromEnv(envPoint));
    }

    /**
     * The method changes the point referred ad 'position'.
     *
     * @param from is a {@link Point2D} into the view-space
     */
    private void setViewPositionWithoutMoving(final PointAdapter<P> from) {
        final PointAdapter<P> envDelta = envPointFromView(from).diff(envPointFromView(position));
        position = from;
        effectCenter = effectCenter.sum(envDelta);
    }

    @Override
    public void setEnvPosition(final Position2D<?> envPoint) {
        setViewPosition(getViewPoint(envPoint));
    }

    @Override
    public abstract void optimalZoom();

    @Override
    public void zoomOnPoint(final Point point, final double zoomRate) {
        final PointAdapter<P> orig = effectCenter;
        setViewPositionWithoutMoving(from(point));
        setZoom(zoomRate);
        setEnvPositionWithoutMoving(orig);
    }

    @Override
    public void center() {
        final double[] off = getEnvironment().getOffset();
        final double[] size = getEnvironment().getSize();
        final PointAdapter<P> center = Double.isNaN(off[0]) || Double.isNaN(off[1]) || size[0] <= 0 || size[1] <= 0
                ? from(0, 0)
                : from(off[0] + size[0] / 2, off[1] + size[1] / 2);
        setEnvPosition(center.toPosition(environment));
    }

    /**
     * Getter method for the {@code Environment} model.
     *
     * @return the {@code Environment} model
     */
    protected final Environment<?, P> getEnvironment() {
        return this.environment;
    }

    /**
     * Getter method for the controlled View.
     *
     * @return the controlled View
     */
    protected final ViewType getView() {
        return this.view;
    }

    /**
     * Getter method for position field.
     *
     * @return the position
     */
    protected final PointAdapter<P> getPosition() {
        return position;
    }

    /**
     * Setter method for position field.
     *
     * @param position the position to set
     */
    protected final void setPosition(final PointAdapter<P> position) {
        this.position = position;
    }

    /**
     * Translates a point to the env space.
     *
     * @param viewPoint view space point
     * @return env space point
     */
    protected final PointAdapter<P> envPointFromView(final PointAdapter<P> viewPoint) {
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
        return this.rotation;
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

    /**
     * Gets the viewWidth / envWidth ratio.
     * <br/>
     * NI = Not Isometric.
     *
     * @return a {@code double} value representing the horizontal ratio for
     * Not Isometric mode
     */
    protected double getNIHorizontalRatio() {
        if (mode == Mode.ISOMETRIC) {
            return 1d;
        } else if (mode == Mode.ADAPT_TO_VIEW) {
            return getViewSize().getWidth() / environment.getSize()[0];
        } else {
            return hRate;
        }
    }

    /**
     * Gets the viewHeight / envHeight ratio.
     * <br/>
     * NI = Not Isometric.
     *
     * @return a <code>double</code> value representing the vertical ratio for
     * Not Isometric mode
     */
    protected double getNIVerticalRatio() {
        if (mode == Mode.ISOMETRIC) {
            return 1d;
        } else if (mode == Mode.ADAPT_TO_VIEW) {
            return getViewSize().getHeight() / environment.getSize()[1];
        } else {
            return vRate;
        }
    }
}
