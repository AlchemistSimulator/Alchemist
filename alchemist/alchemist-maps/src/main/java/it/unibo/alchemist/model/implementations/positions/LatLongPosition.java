/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.positions; // NOPMD by danysk on 2/4/14 3:39 PM

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.acos;
import static org.apache.commons.math3.util.FastMath.asin;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.apache.commons.math3.util.FastMath.toDegrees;
import static org.apache.commons.math3.util.FastMath.toRadians;

import java.util.List;

import org.danilopianini.lang.HashUtils;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.model.interfaces.Position;

import com.google.common.collect.Lists;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unmodifiable state version of {@link LatLng}, also implementing the
 * {@link Position} interface.
 * 
 */
public final class LatLongPosition implements Position {

    /**
     * The default distance formula.
     */
    public static final DistanceFormula DEFAULT_DISTANCE_FORMULA = DistanceFormula.EQUIRECTANGULAR;
    /**
     * Mean Earth radius in meters.
     */
    public static final double EARTH_MEAN_RADIUS_METERS = 6371009;
    private static final double MAX_LAT = Math.toRadians(90d); // PI/2
    private static final double MAX_LON = Math.toRadians(180d); // PI
    private static final double MIN_LAT = Math.toRadians(-90d); // -PI/2
    private static final double MIN_LON = Math.toRadians(-180d); // -PI

    private static final long serialVersionUID = -8972065367390749356L;

    private final DistanceFormula df;
    private final LatLng latlng;
    private int hash;

    /**
     * Possible methods to compute the distance between two latitude-longitude
     * points.
     * 
     */
    public enum DistanceFormula {

        /**
         * 
         */
        EQUIRECTANGULAR, HAVERSINE, SPHERICAL_COSINES
    }

    /**
     * Distance between two points.
     * 
     * @param point1
     *            the first point.
     * @param point2
     *            the second point.
     * @param df
     *            the formula to use to compute distances
     * @return the distance in the chosen unit of measure.
     */
    public static double distance(final LatLng point1, final LatLng point2, final DistanceFormula df) {
        return distanceInRadians(point1, point2, df) * EARTH_MEAN_RADIUS_METERS;
    }

    /**
     * Distance between two points with arbitrary {@link LengthUnit}.
     * 
     * @param point1
     *            the first point.
     * @param point2
     *            the second point.
     * @param unit
     *            the unit of measure in which to receive the result.
     * @param df
     *            the formula to use to compute distances
     * @return the distance in the chosen unit of measure.
     */
    public static double distance(final LatLng point1, final LatLng point2, final LengthUnit unit, final DistanceFormula df) {
        return LengthUnit.METER.convertTo(unit, distance(point1, point2, df));
    }

    /**
     * <p>
     * This "distance" function is mostly for internal use. Most users will
     * simply rely upon {@link #distance(LatLng, LatLng, LengthUnit, DistanceFormula)}
     * </p>
     * 
     * <p>
     * Yields the internal angle for an arc between two points on the surface of
     * a sphere in radians. This angle is in the plane of the great circle
     * connecting the two points measured from an axis through one of the points
     * and the center of the Earth. Multiply this value by the sphere's radius
     * to get the length of the arc.
     * </p>
     * 
     * @param point1
     *            the first point
     * @param point2
     *            the second point
     * @param precision
     *            the formula to use
     * @return the internal angle for the arc connecting the two points in
     *         radians.
     */
    public static double distanceInRadians(final LatLng point1, final LatLng point2, final DistanceFormula precision) {
        final double lat1R = toRadians(point1.getLatitude());
        final double lat2R = toRadians(point2.getLatitude());
        final double lon1R = toRadians(point1.getLongitude());
        final double lon2R = toRadians(point2.getLongitude());
        switch (precision) {
        case HAVERSINE:
            final double dLatR = abs(lat2R - lat1R);
            final double dLngR = abs(lon2R - lon1R);
            final double a = sin(dLatR / 2) * sin(dLatR / 2) + cos(lat1R) * cos(lat2R) * sin(dLngR / 2) * sin(dLngR / 2);
            return 2 * atan2(sqrt(a), sqrt(1 - a));
        case SPHERICAL_COSINES:
            return acos(sin(lat1R) * sin(lat2R) + cos(lat1R) * cos(lat2R) * cos(abs(lon2R - lon1R)));
        case EQUIRECTANGULAR:
            final double x = (lon2R - lon1R) * cos((lat1R + lat2R) / 2);
            final double y = lat2R - lat1R;
            return sqrt(x * x + y * y);
        default:
            throw new IllegalStateException("Unknown algorithm required: " + precision);
        }
    }

    /**
     * @param lat
     *            latitude
     * @param lon
     *            longitude
     */
    public LatLongPosition(final double lat, final double lon) {
        this(lat, lon, DEFAULT_DISTANCE_FORMULA);
    }

    /**
     * @param lat
     *            latitude
     * @param lon
     *            longitude
     * @param distanceFormula
     *            the formula to use to compute distances
     */
    public LatLongPosition(final double lat, final double lon, final DistanceFormula distanceFormula) {
        latlng = new LatLng(lat, lon);
        df = distanceFormula;
    }

    /**
     * @param lat
     *            latitude
     * @param lon
     *            longitude
     * @param distanceFormula
     *            the index of the formula to use to compute distances
     */
    public LatLongPosition(final double lat, final double lon, final int distanceFormula) {
        this(lat, lon, DistanceFormula.values()[distanceFormula % DistanceFormula.values().length]);
    }

    /**
     * @param lat
     *            latitude
     * @param lon
     *            longitude
     */
    public LatLongPosition(final Number lat, final Number lon) {
        this(lat.doubleValue(), lon.doubleValue());
    }

    @Override
    public List<Position> buildBoundingBox(final double range) {
        if (range < 0d) {
            throw new IllegalArgumentException("Negative ranges make no sense.");
        }
        /*
         *  angular distance in radians on a great circle
         */
        final double radDist = range / EARTH_MEAN_RADIUS_METERS;
        final double radLat = toRadians(getLatitude());
        final double radLon = toRadians(getLongitude());

        double minLat = radLat - radDist;
        double maxLat = radLat + radDist;

        double minLon;
        double maxLon;
        if (minLat > MIN_LAT && maxLat < MAX_LAT) {
            final double deltaLon = asin(Math.sin(radDist) / cos(radLat));
            minLon = radLon - deltaLon;
            if (minLon < MIN_LON) {
                minLon += 2d * Math.PI;
            }
            maxLon = radLon + deltaLon;
            if (maxLon > MAX_LON) {
                maxLon -= 2d * Math.PI;
            }
        } else {
            // a pole is within the distance
            minLat = Math.max(minLat, MIN_LAT);
            maxLat = Math.min(maxLat, MAX_LAT);
            minLon = MIN_LON;
            maxLon = MAX_LON;
        }
        return Lists.newArrayList(
                new LatLongPosition(toDegrees(minLat), toDegrees(minLon)),
                new LatLongPosition(toDegrees(maxLat), toDegrees(maxLon)));
    }

    @Override
    public int compareTo(final Position o) {
        if (getDimensions() < o.getDimensions()) {
            return -1;
        }
        if (getDimensions() > o.getDimensions()) {
            return 1;
        }
        final double[] pos = o.getCartesianCoordinates();
        final double[] c = getCartesianCoordinates();
        for (int i = 0; i < c.length; i++) {
            if (c[i] < pos[i]) {
                return -1;
            }
            if (c[i] > pos[i]) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public double[] getCartesianCoordinates() {
        return new double[] { getLongitude(), getLatitude() };
    }

    @Override
    public double getCoordinate(final int dim) {
        if (dim == 0) {
            return getLongitude();
        }
        if (dim == 1) {
            return getLatitude();
        }
        throw new IllegalArgumentException("Pass 0 for longitude and 1 for latitude. No other value accepted.");
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latlng.getLatitude();
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return latlng.getLongitude();
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public double getDistanceTo(final Position p) {
        if (p instanceof LatLongPosition) {
            return distance(latlng, ((LatLongPosition) p).latlng, df);
        }
        final int pDims = p.getDimensions();
        if (pDims == 2) {
            final double[] coords = p.getCartesianCoordinates();
            return distance(latlng, new LatLng(coords[1], coords[0]), df);
        }
        throw new UncomparableDistancesException(this, p);
    }

    @Override
    @SuppressFBWarnings(justification = "Exact floating point equality is required here.")
    public boolean equals(final Object obj) {
        if (obj instanceof LatLongPosition) {
            final LatLongPosition llp = (LatLongPosition) obj;
            return getLatitude() == llp.getLatitude() && getLongitude() == llp.getLongitude();
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = HashUtils.hash32(getLatitude(), getLongitude());
        }
        return hash;
    }

    @Override
    public String toString() {
        return latlng.toString();
    }

    @Override
    public Position sum(final Position other) {
        if (other instanceof LatLongPosition) {
            final LatLng l = ((LatLongPosition) other).latlng;
            return new LatLongPosition(
                    latlng.getLatitude() + l.getLatitude(),
                    latlng.getLongitude() + l.getLongitude());
        }
        throw new IllegalArgumentException(
                "You are summing a " + getClass() + "with a " + other.getClass() + ". This is not supported.");
    }

}
