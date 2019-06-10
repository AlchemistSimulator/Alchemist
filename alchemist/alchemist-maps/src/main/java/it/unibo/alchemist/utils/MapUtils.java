/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.utils;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.GeoPosition;

/**
 */
public final class MapUtils {

    private MapUtils() {
    }

    /**
     * Returns the distance in meters.
     * 
     * @param lat1
     *            Latitude of first point, in decimal degrees.
     * @param lon1
     *            Longitude of first point, in decimal degrees.
     * @param lat2
     *            Latitude of second point, in decimal degrees.
     * @param lon2
     *            Longitude of second point, in decimal degrees.
     * @return the distance in meters
     */
    public static double getDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        return LatLngTool.distance(new LatLng(lat1, lon1), new LatLng(lat2, lon2), LengthUnit.METER);
    }

    /**
     * Returns the distance in meters.
     * 
     * @param p1
     *            First point
     * @param p2
     *            Second point
     * @return the distance in meters
     */
    public static double getDistance(final GeoPosition p1, final GeoPosition p2) {
        return LatLngTool.distance(toLatLng(p1), toLatLng(p2), LengthUnit.METER);
    }

    /**
     * @param start
     *            initial position
     * @param end
     *            final position
     * @return the initial bearing
     */
    public static double initialBearing(final GeoPosition start, final GeoPosition end) {
        return LatLngTool.initialBearing(toLatLng(start), toLatLng(end));
    }

    /**
     * @param start
     *            initial position
     * @param initialBearing
     *            the initial bearing
     * @param dist
     *            maximum walkable length
     * @return the actual destination
     */
    public static LatLongPosition getDestinationLocation(final GeoPosition start, final double initialBearing, final double dist) {
        return toLatLong(LatLngTool.travel(toLatLng(start), initialBearing, dist, LengthUnit.METER));
    }

    /**
     * @param start
     *            initial position
     * @param end
     *            final position
     * @param dist
     *            maximum walkable length
     * @return the actual destination
     */
    public static LatLongPosition getDestinationLocation(final GeoPosition start, final GeoPosition end, final double dist) {
        final double bearing = initialBearing(start, end);
        return getDestinationLocation(start, bearing, dist);
    }

    /**
     * Converts {@link GeoPosition} to {@link LatLng}.
     * 
     * @param p
     *            the {@link GeoPosition}
     * @return a {@link LatLng}
     */
    public static LatLng toLatLng(final GeoPosition p) {
        return new LatLng(p.getLatitude(), p.getLongitude());
    }

    /**
     * Builds a new {@link LatLng}.
     * 
     * @param x
     *            the longitude
     * @param y
     *            the latitude
     * @return a new {@link LatLng}
     */
    public static LatLng toLatLng(final double x, final double y) {
        return new LatLng(y, x);
    }

    /**
     * Converts {@link LatLng} to {@link LatLongPosition}.
     * 
     * @param p
     *            {@link LatLng}
     * @return a {@link LatLongPosition}
     */
    public static LatLongPosition toLatLong(final LatLng p) {
        return new LatLongPosition(p.getLatitude(), p.getLongitude());
    }

    /**
     * Builds a new {@link LatLongPosition}.
     * 
     * @param x
     *            the longitude
     * @param y
     *            the latitude
     * @return a new {@link LatLongPosition}
     */
    public static LatLongPosition toLatLong(final double x, final double y) {
        return toLatLong(toLatLng(x, y));
    }

}
