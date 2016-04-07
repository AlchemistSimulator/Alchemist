/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.wormhole.implementation;

import static it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter.from;

import java.awt.Component;
import java.awt.Point;
import java.util.function.BiFunction;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.model.MapViewPosition;

import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Wormhole used for maps rendering.
 * 

 */
public final class MapWormhole extends Wormhole2D {
    private final MapViewPosition mapModel;
    /**
     * Maximum zoom.
     */
    public static final byte MAX_ZOOM = 18;
    private static final long MAPSFORGE_TILE_SIZE = 256;

    /**
     * Initializes a new {@link MapWormhole} copying the state of the one in
     * input.
     * 
     * @param env
     *            the {@link Environment}
     * @param comp
     *            the controlled {@link Component}
     * @param m
     *            the {@link MapViewPosition}
     */
    public MapWormhole(final Environment<?> env, final Component comp, final MapViewPosition m) {
        super(env, comp);
        mapModel = m;
        super.setMode(Mode.MAP);
    }

    @Override
    public Position getEnvPoint(final Point viewPoint) {
        final LatLong l = mapModel.getCenter();
        final PointAdapter c = coordToPx(from(l.longitude, l.latitude));
        final PointAdapter d = from(viewPoint).diff(from(getViewPosition()));
        final PointAdapter p = d.sum(c);
        if (p.getX() < 0 || p.getY() < 0 || p.getX() > mapSize() || p.getY() > mapSize()) {
            /*
             * The point is OUTSIDE the map.
             */
            return new LatLongPosition(l.latitude, l.longitude);
        }
        return new LatLongPosition(pxYToLat(p.getY()), pxXToLon(p.getX()));
    }

    private double lonToPxX(final double lon) {
        return mercatorApplier(MercatorProjection::longitudeToPixelX, lon);
    }

    private double pxXToLon(final double pxx) {
        return mercatorApplier(MercatorProjection::pixelXToLongitude, pxx);
    }

    private double latToPxY(final double lat) {
        return mercatorApplier(MercatorProjection::latitudeToPixelY, lat);
    }

    private double pxYToLat(final double pxy) {
        return mercatorApplier(MercatorProjection::pixelYToLatitude, pxy);
    }

    private double mercatorApplier(final BiFunction<Double, Long, Double> fun, final double arg) {
        return fun.apply(arg, mapSize());
    }

    private long mapSize() {
        return MAPSFORGE_TILE_SIZE << mapModel.getZoomLevel();
    }

    private PointAdapter coordToPx(final PointAdapter pt) {
        return from(lonToPxX(pt.getX()), latToPxY(pt.getY()));
    }

    @Override
    public Point getViewPoint(final Position envPoint) {
        final LatLong l = mapModel.getCenter();
        final PointAdapter viewPoint = coordToPx(from(envPoint));
        final PointAdapter centerView = coordToPx(from(l.longitude, l.latitude));
        final PointAdapter diff = viewPoint.diff(centerView);
        final PointAdapter vc = from(getViewPosition());
        return vc.sum(diff).toPoint();
    }

    @Override
    public Point getViewPosition() {
        return from(getViewSize().getWidth() / 2, getViewSize().getHeight() / 2).toPoint();
    }

    @Override
    public void rotateAroundPoint(final Point p, final double a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnvPosition(final Position ep) {
        LatLong center;
        try {
            center = new LatLong(ep.getCoordinate(1), ep.getCoordinate(0));
        } catch (IllegalArgumentException e) {
            center = new LatLong(0, 0);
        }
        mapModel.setCenter(center);
    }

    @Override
    public void optimalZoom() {
        byte zoom = MAX_ZOOM;
        @SuppressWarnings("unchecked")
        final Environment<Object> env = (Environment<Object>) getEnvironment();
        do {
            setZoom(zoom);
            zoom--;
        } while (zoom > 1 && !env.getNodes().parallelStream()
                .map(env::getPosition)
                .map(this::getViewPoint)
                .allMatch(this::isInsideView));
    }

    @Override
    public void setViewPosition(final Point p) {
        final PointAdapter pt = from(p).diff(from(getViewPosition()));
        mapModel.moveCenter(pt.getX(), pt.getY());
    }

    @Override
    public void setZoom(final double z) {
        super.setZoom(z);
        mapModel.setZoomLevel((byte) getZoom());
    }

    @Override
    public void zoomOnPoint(final Point zoomPoint, final double z) {
        final PointAdapter endPoint = envPointFromView(from(zoomPoint));
        setZoom(z);
        final PointAdapter newViewCenter = viewPointFromEnv(endPoint);
        final PointAdapter delta = from(zoomPoint).diff(newViewCenter);
        setViewPosition(from(getViewPosition()).sum(delta).toPoint());
    }

}
