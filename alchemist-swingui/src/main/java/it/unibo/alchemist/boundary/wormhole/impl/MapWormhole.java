/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.wormhole.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.ui.impl.PointAdapter;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.maps.positions.LatLongPosition;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.model.MapViewPosition;

import java.awt.Component;
import java.awt.Point;
import java.util.function.BiFunction;

import static it.unibo.alchemist.boundary.ui.impl.PointAdapter.from;

/**
 * Wormhole used for maps rendering.
 *
 */
public final class MapWormhole extends WormholeSwing<GeoPosition> {

    /**
     * Maximum zoom.
     */
    public static final byte MAX_ZOOM = 18;

    private static final long MAPSFORGE_TILE_SIZE = 256;

    private final MapViewPosition mapModel;

    /**
     * Initializes a new {@link MapWormhole} copying the state of the one in
     * input.
     *
     * @param environment
     *            the {@link Environment}
     * @param comp
     *            the controlled {@link Component}
     * @param m
     *            the {@link MapViewPosition}
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public MapWormhole(final Environment<?, GeoPosition> environment, final Component comp, final MapViewPosition m) {
        super(environment, comp);
        mapModel = m;
        super.setMode(Mode.MAP);
    }

    @Override
    public GeoPosition getEnvPoint(final Point viewPoint) {
        final LatLong l = mapModel.getCenter();
        final PointAdapter<GeoPosition> c = coordToPx(from(l.longitude, l.latitude));
        final PointAdapter<GeoPosition> d = PointAdapter.<GeoPosition>from(viewPoint).diff(from(getViewPosition()));
        final PointAdapter<GeoPosition> p = d.sum(c);
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

    private PointAdapter<GeoPosition> coordToPx(final PointAdapter<GeoPosition> pt) {
        return from(lonToPxX(pt.getX()), latToPxY(pt.getY()));
    }

    @Override
    public Point getViewPoint(final GeoPosition environmentPoint) {
        final LatLong l = mapModel.getCenter();
        final PointAdapter<GeoPosition> viewPoint = coordToPx(from(environmentPoint));
        final PointAdapter<GeoPosition> centerView = coordToPx(from(l.longitude, l.latitude));
        final PointAdapter<GeoPosition> diff = viewPoint.diff(centerView);
        final PointAdapter<GeoPosition> vc = from(getViewPosition());
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
    public void setEnvPosition(final GeoPosition ep) {
        LatLong center;
        try {
            center = new LatLong(ep.getLatitude(), ep.getLongitude());
        } catch (final IllegalArgumentException e) {
            center = new LatLong(0, 0);
        }
        mapModel.setCenter(center);
    }

    @Override
    public void optimalZoom() {
        byte zoom = MAX_ZOOM;
        @SuppressWarnings("unchecked")
        final Environment<Object, GeoPosition> environment = (Environment<Object, GeoPosition>) getEnvironment();
        do {
            setZoom(zoom);
            zoom--;
        } while (zoom > 1 && !environment.getNodes().parallelStream()
                .map(environment::getPosition)
                .map(this::getViewPoint)
                .allMatch(this::isInsideView));
    }

    @Override
    public void setViewPosition(final Point p) {
        final PointAdapter<GeoPosition> pt = PointAdapter.<GeoPosition>from(p).diff(from(getViewPosition()));
        mapModel.moveCenter(pt.getX(), pt.getY());
    }

    @Override
    public void setZoom(final double z) {
        super.setZoom(z);
        mapModel.setZoomLevel((byte) getZoom());
    }

    @Override
    public void zoomOnPoint(final Point zoomPoint, final double z) {
        final PointAdapter<GeoPosition> endPoint = envPointFromView(from(zoomPoint));
        setZoom(z);
        final PointAdapter<GeoPosition> newViewCenter = viewPointFromEnv(endPoint);
        final PointAdapter<GeoPosition> delta = PointAdapter.<GeoPosition>from(zoomPoint).diff(newViewCenter);
        setViewPosition(from(getViewPosition()).sum(delta).toPoint());
    }
}
