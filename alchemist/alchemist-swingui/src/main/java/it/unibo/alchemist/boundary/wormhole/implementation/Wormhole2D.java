/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.implementation;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;
import static java.lang.Double.isNaN;

/**
 * Partial implementation for the interface {@link IWormhole2D}.<br>
 * I am considering the particular case of the view as an entity into the
 * screen-space: the y-axis grows on the bottom side of the screen.
 *
 * @param <P> position type
 */
public class Wormhole2D<P extends Position2D<? extends P>> implements IWormhole2D<P> {

    private static final Logger L = LoggerFactory.getLogger(Wormhole2D.class);
    private double angle;
    private PointAdapter<P> effectCenter = from(0, 0);
    private double hRate = 1d;
    private Mode mode = Mode.ISOMETRIC;
    private final Environment<?, P> model;
    private PointAdapter<P> position;
    private final Component view;
    private double vRate = 1d;
    private double zoom = 1d;

    /**
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     * 
     * @param env the {@link Environment}
     * @param comp the controlled {@link Component}
     */
    public Wormhole2D(final Environment<?, P> env, final Component comp) {
        model = env;
        view = comp;
        position = from(comp.getWidth() / 2.0, comp.getHeight() / 2.0);
    }

    /**
     * Calculates the {@link AffineTransform} that allows the wormhole to
     * convert points from env-space to view-space.
     * 
     * @return an {@link AffineTransform} object
     */
    protected AffineTransform calculateTransform() {
        final AffineTransform t;
        if (getMode() == Mode.ISOMETRIC) {
            t = new AffineTransform(
                    getZoom(),  0d,                       0d,
                    -getZoom(), getViewPosition().getX(), getViewPosition().getY());
        } else {
            t = new AffineTransform(getZoom() * getHRate(), 0d, 0d, -getZoom() * getVRate(), getViewPosition().getX(), getViewPosition().getY());
        }
        t.concatenate(AffineTransform.getRotateInstance(getRotation()));
        return t;
    }

    @Override
    public final void center() {
        final double[] off = getEnvironment().getOffset();
        final double[] size = getEnvironment().getSize();
        final PointAdapter<P> center = isNaN(off[0]) || isNaN(off[1]) || size[0] <= 0 || size[1] <= 0
                ? from(0, 0)
                : from(off[0] + size[0] / 2, off[1] + size[1] / 2);
        setEnvPosition(center.toPosition().orElse(makePosition(center.getX(), center.getY())));
    }

    /**
     * Translates a point to the env space.
     * 
     * @param viewPoint
     *            view space point
     * @return env space point
     */
    protected final PointAdapter<P> envPointFromView(final PointAdapter<P> viewPoint) {
        final Point2D.Double vp = new Point2D.Double(viewPoint.toPoint().x, viewPoint.toPoint().y);
        final AffineTransform t = calculateTransform();
        try {
            t.inverseTransform(vp, vp);
        } catch (final NoninvertibleTransformException e) {
            L.error("Unable to perform the transformation from view point to env point. Please check if this method has been called after making the UI visible", e);
        }
        return from(vp);
    }

    /**
     * @return the {@link Environment}
     */
    protected final Environment<?, P> getEnvironment() {
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public P getEnvPoint(final Point viewPoint) {
        final PointAdapter<P> adapter = envPointFromView(from(viewPoint));
        return adapter.toPosition().orElse(makePosition(adapter.getX(), adapter.getY()));
    }

    private double getEnvRatio() {
        final double[] size = model.getSize();
        return size[0] / size[1];
    }

    /**
     * @return the horizontal stretch rate
     */
    protected double getHRate() {
        return hRate;
    }

    @Override
    public final Mode getMode() {
        return mode;
    }

    /**
     * Gets the viewWidth / envWidth ratio.<br>
     * NI = Not Isometric.
     * 
     * @return a <code>double</code> value representing the horizontal ratio for
     *         Not Isometric mode
     */
    private double getNIHorizontalRatio() {
        if (mode == Mode.ISOMETRIC) {
            return 1d;
        } else if (mode == Mode.ADAPT_TO_VIEW) {
            return view.getWidth() / model.getSize()[0];
        } else {
            return hRate;
        }
    }

    /**
     * Gets the viewHeight / envHeight ratio.<br>
     * NI = Not Isometric.
     * 
     * @return a <code>double</code> value representing the vertical ratio for
     *         Not Isometric mode
     */
    private double getNIVerticalRatio() {
        if (mode == Mode.ISOMETRIC) {
            return 1d;
        } else if (mode == Mode.ADAPT_TO_VIEW) {
            return view.getHeight() / model.getSize()[1];
        } else {
            return vRate;
        }
    }

    /**
     * Gets the rotation angle, in radians.
     * 
     * @return a <code>double</code> value representing an angle expressed with
     *         radians
     * @see #setRotation(double)
     */
    protected double getRotation() {
        return angle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getViewPoint(final P envPoint) {
        return viewPointFromEnv(from(envPoint)).toPoint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getViewPosition() {
        return position.toPoint();
    }

    private double getViewRatio() {
        final Dimension size = view.getSize();
        return Math.max(1, size.getWidth()) / Math.max(1, size.getHeight());
    }

    @Override
    public final Dimension2D getViewSize() {
        return view.getSize();
    }

    /**
     * Gets the vertical stretch rate.
     * 
     * @return a <code>double</code> value representing the vertical stretch
     *         rate
     */
    protected double getVRate() {
        return vRate;
    }

    @Override
    public final double getZoom() {
        return zoom;
    }

    @Override
    public final boolean isInsideView(final Point viewPoint) {
        final double x = viewPoint.getX();
        final double y = viewPoint.getY();
        final Dimension2D vs = getViewSize();
        return x >= 0 && x <= vs.getWidth() && y >= 0 && y <= vs.getHeight();
    }

    /**
     * Creates a new position by delegating to the environment.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return a new position
     */
    protected P makePosition(final double x, final double y) {
        return getEnvironment().makePosition(x, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void optimalZoom() {
        if (getEnvRatio() <= getViewRatio()) {
            zoom = Math.max(1, view.getHeight()) / model.getSize()[1];
        } else {
            zoom = Math.max(1, view.getWidth()) / model.getSize()[0];
        }

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
     * {@inheritDoc}
     */
    @Override
    public void setEnvPosition(final P pos) {
        setViewPosition(getViewPoint(pos));
    }

    /**
     * Changes the point referred ad 'position'.
     * 
     * @param envPoint
     *            is a {@link Point2D} into the env-space
     */
    private void setEnvPositionWithoutMoving(final PointAdapter<P> envPoint) {
        setViewPositionWithoutMoving(viewPointFromEnv(envPoint));
    }

    /**
     * Allows child-classes to modify the {@link #mode} field.
     * 
     * @param m
     *            is the new {@link #mode}
     */
    protected void setMode(final Mode m) {
        mode = m;
        if (m == Mode.ADAPT_TO_VIEW) {
            vRate = getNIVerticalRatio();
            hRate = getNIHorizontalRatio();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRotation(final double rad) {
        angle = rad % (Math.PI * 2d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewPosition(final Point point) {
        position = from(point);
    }

    /**
     * Changes the point referred ad 'position'.
     * 
     * @param viewPoint
     *            is a {@link Point2D} into the view-space
     */
    private void setViewPositionWithoutMoving(final PointAdapter<P> viewPoint) {
        final PointAdapter<P> envDelta = envPointFromView(viewPoint).diff(envPointFromView(position));
        position = viewPoint;
        effectCenter = effectCenter.sum(envDelta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setZoom(final double value) {
        if (value <= 0d) {
            zoom = 0d;
        }
        zoom = value;
    }

    /**
     * Translates a point to the view space.
     * 
     * @param envPoint
     *            env space point
     * @return view space point
     */
    protected final PointAdapter<P> viewPointFromEnv(final PointAdapter<P> envPoint) {
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
    public void zoomOnPoint(final Point p, final double z) {
        final PointAdapter<P> orig = effectCenter;
        setViewPositionWithoutMoving(from(p));
        setZoom(z);
        setEnvPositionWithoutMoving(orig);
    }

}
