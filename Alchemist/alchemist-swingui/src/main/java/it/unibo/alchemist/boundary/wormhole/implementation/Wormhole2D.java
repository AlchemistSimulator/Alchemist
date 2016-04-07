/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;
import static java.lang.Double.isNaN;

/**
 * Partial implementation for the interface {@link IWormhole2D}.<br>
 * I am considering the particular case of the view as an entity into the
 * sceern-space: the y-axis grows on the bottom side of the screen.
 * 
 */
public class Wormhole2D implements IWormhole2D {

    private final Environment<?> model;
    private final Component view;
    private PointAdapter position;
    private PointAdapter effectCenter = from(0, 0);
    private double zoom = 1d;
    private double angle;
    private double hRate = 1d;
    private double vRate = 1d;
    private Mode mode = Mode.ISOMETRIC;
    private static final Logger L = LoggerFactory.getLogger(Wormhole2D.class);

    /**
     * Initializes a new instance directly setting the size of both view and
     * environment, and the offset too.
     * 
     * @param env the {@link Environment}
     * @param comp the controlled {@link Component}
     */
    public Wormhole2D(final Environment<?> env, final Component comp) {
        model = env;
        view = comp;
        position = from(comp.getWidth() / 2, comp.getHeight() / 2);
    }

    private double getEnvRatio() {
        final double[] size = model.getSize();
        return size[0] / size[1];
    }

    private double getViewRatio() {
        final Dimension size = view.getSize();
        return size.getWidth() / size.getHeight();
    }

    /**
     * @return the horizontal stretch rate
     */
    protected double getHRate() {
        return hRate;
    }

    @Override
    public Mode getMode() {
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

    @Override
    public Point getViewPosition() {
        return position.toPoint();
    }

    @Override
    public Dimension2D getViewSize() {
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
    public double getZoom() {
        return zoom;
    }

    @Override
    public boolean isInsideView(final Point viewPoint) {
        final double x = viewPoint.getX();
        final double y = viewPoint.getY();
        final Dimension2D vs = getViewSize();
        return x >= 0 && x <= vs.getWidth() && y >= 0 && y <= vs.getHeight();
    }

    @Override
    public void setEnvPosition(final Position pos) {
        setViewPosition(getViewPoint(pos));
    }

    /**
     * Changes the point referred ad 'position'.
     * 
     * @param envPoint
     *            is a {@link Point2D} into the env-space
     */
    private void setEnvPositionWithoutMoving(final PointAdapter envPoint) {
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

    @Override
    public void optimalZoom() {
        if (getEnvRatio() <= getViewRatio()) {
            zoom = view.getHeight() / model.getSize()[1];
        } else {
            zoom = view.getWidth() / model.getSize()[0];
        }

    }

    @Override
    public void setRotation(final double rad) {
        angle = rad % (Math.PI * 2d);
    }

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
    private void setViewPositionWithoutMoving(final PointAdapter viewPoint) {
        final PointAdapter envDelta = envPointFromView(viewPoint).diff(envPointFromView(position));
        position = viewPoint;
        effectCenter = effectCenter.sum(envDelta);
    }

    @Override
    public void rotateAroundPoint(final Point p, final double a) {
        final PointAdapter orig = effectCenter;
        setViewPositionWithoutMoving(from(p));
        setRotation(a);
        setEnvPositionWithoutMoving(orig);
    }

    @Override
    public void zoomOnPoint(final Point p, final double z) {
        final PointAdapter orig = effectCenter;
        setViewPositionWithoutMoving(from(p));
        setZoom(z);
        setEnvPositionWithoutMoving(orig);
    }

    @Override
    public void setZoom(final double value) {
        if (value < 0d) {
            zoom = 0d;
        }
        zoom = value;
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
            t = new AffineTransform(getZoom(), 0d, 0d, -getZoom(), getViewPosition().getX(), getViewPosition().getY());
        } else {
            t = new AffineTransform(getZoom() * getHRate(), 0d, 0d, -getZoom() * getVRate(), getViewPosition().getX(), getViewPosition().getY());
        }
        t.concatenate(AffineTransform.getRotateInstance(getRotation()));
        return t;
    }

    @Override
    public Position getEnvPoint(final Point viewPoint) {
        return envPointFromView(from(viewPoint)).toPosition();
    }

    /**
     * Translates a point to the env space.
     * 
     * @param viewPoint
     *            view space point
     * @return env space point
     */
    protected final PointAdapter envPointFromView(final PointAdapter viewPoint) {
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
     * Translates a point to the view space.
     * 
     * @param envPoint
     *            env space point
     * @return view space point
     */
    protected final PointAdapter viewPointFromEnv(final PointAdapter envPoint) {
        final PointAdapter envp = envPoint.diff(effectCenter);
        final Point2D ep = envp.toPoint2D();
        final AffineTransform t = calculateTransform();
        t.transform(ep, ep);
        return from(ep);
    }

    @Override
    public Point getViewPoint(final Position envPoint) {
        return viewPointFromEnv(from(envPoint)).toPoint();
    }

    /**
     * @return the {@link Environment}
     */
    protected final Environment<?> getEnvironment() {
        return model;
    }

    @Override
    public void center() {
        final double[] off = getEnvironment().getOffset();
        final double[] size = getEnvironment().getSize();
        final PointAdapter center = isNaN(off[0]) || isNaN(off[1]) || size[0] <= 0 || size[1] <= 0
                ? from(0, 0)
                : from(off[0] + size[0] / 2, off[1] + size[1] / 2);
        setEnvPosition(center.toPosition());
    }

}
