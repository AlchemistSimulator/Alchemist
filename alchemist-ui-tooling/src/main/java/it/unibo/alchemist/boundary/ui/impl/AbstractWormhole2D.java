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
import it.unibo.alchemist.boundary.ui.api.ViewPort;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;

import javax.annotation.Nonnull;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.function.Function;

import static it.unibo.alchemist.boundary.ui.impl.PointAdapter.from;

/**
 * Partial, abstract, implementation for the interface {@link Wormhole2D}.
 *
 * This implementation considers the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 *
 * This abstract class is independent from the 2D graphical component wrapped.
 *
 * @param <P> the position type
 */
public abstract class AbstractWormhole2D<P extends Position2D<? extends P>> implements Wormhole2D<P> {
    private final static double ZOOM_FACTOR = 0.10;
    private final Environment<?, P> environment;
    private final ViewPort view;
    private PointAdapter<P> position;
    private double zoom = 1d;
    private double hRate = 1d;
    private double vRate = 1d;
    private double rotation;
    private Mode mode = Mode.ISOMETRIC;
    private PointAdapter<P> effectCenter = from(0, 0);

    /**
     * Wormhole constructor for any {@link ViewPort}.
     *
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     *
     * @param <T>                    the type of the viewType
     * @param environment            the {@link Environment}
     * @param view                   the {@link ViewPort} of the UI used for implementing the wormhole.
     * @param viewTypeToPointAdapter a {@link Function} used to create the initial position of the wormhole.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public <T extends ViewPort> AbstractWormhole2D(
        @Nonnull final Environment<?, P> environment,
        @Nonnull final T view,
        @Nonnull final Function<T, PointAdapter<P>> viewTypeToPointAdapter
    ) {
        this.environment = environment;
        this.view = view;
        this.position = viewTypeToPointAdapter.apply(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public P getEnvPoint(final Point viewPoint) {
        return envPointFromView(from(viewPoint)).toPosition(environment);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getViewPoint(final P envPoint) {
        return viewPointFromEnv(from(envPoint)).toPoint();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getViewPosition() {
        return position.toPoint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewPosition(final Point viewPoint) {
        this.position = from(viewPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension2D getViewSize() {
        return new DoubleDimension(getView().getWidth(), getView().getHeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getZoom() {
        return this.zoom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setZoom(final double zoom) {
        if (zoom <= 0) {
            this.zoom = 0d;
        }
        this.zoom = zoom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInsideView(final Point viewPoint) {
        final double x = viewPoint.getX();
        final double y = viewPoint.getY();
        final Dimension2D vs = getViewSize();
        return x >= 0 && x <= vs.getWidth() && y >= 0 && y <= vs.getHeight();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnvPosition(final P envPoint) {
        setViewPosition(getViewPoint(envPoint));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimalZoom() {
        final var size = getViewSize();
        final PointAdapter<P> viewCenter = from(size.getWidth() / 2, size.getHeight() / 2);
        final var zoom = getEnvRatio() <= getViewRatio()
            ? Math.max(1, getView().getHeight()) / getEnvironment().getSize()[1]
            : Math.max(1, getView().getWidth()) / getEnvironment().getSize()[0];
        final var adjustedZoom = getEnvRatio() <= getViewRatio()
            ? zoom * (1 - ZOOM_FACTOR) : zoom * (1 + ZOOM_FACTOR);
        zoomOnPoint(viewCenter.toPoint(), adjustedZoom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void zoomOnPoint(final Point point, final double zoomRate) {
        final PointAdapter<P> orig = effectCenter;
        setViewPositionWithoutMoving(from(point));
        setZoom(zoomRate);
        setEnvPositionWithoutMoving(orig);
    }

    /**
     * {@inheritDoc}
     */
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
    protected final ViewPort getView() {
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
            throw new IllegalStateException("Unable to perform the transformation from view point to env point."
                    + " Please check if this method has been called after making the UI visible", e);
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
                    getZoom(),
                    0d,
                    0d,
                    -getZoom(),
                    getViewPosition().getX(),
                    getViewPosition().getY()
            );
        } else {
            t = new AffineTransform(
                    getZoom() * getHRate(),
                    0d,
                    0d,
                    -getZoom() * getVRate(),
                    getViewPosition().getX(),
                    getViewPosition().getY()
            );
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

    /**
     *
     */
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
    protected double getViewRatio() {
        return Math.max(1, getView().getWidth()) / Math.max(1, getView().getHeight());
    }

    /**
     * Gets the viewWidth / envWidth ratio.
     *
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
     *
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
