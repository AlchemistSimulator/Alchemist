/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.positions;

import com.google.common.collect.Lists;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.GeoPosition;
import org.danilopianini.util.Hashes;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BinaryOperator;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.acos;
import static org.apache.commons.math3.util.FastMath.asin;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.apache.commons.math3.util.FastMath.toDegrees;
import static org.apache.commons.math3.util.FastMath.toRadians;

/**
 * Unmodifiable state version of {@link LatLng}, also implementing the
 * {@link GeoPosition} interface.
 * 
 */
public final class LatLongPosition implements GeoPosition {

    /**
     * The default distance formula.
     */
    public static final DistanceFormula DEFAULT_DISTANCE_FORMULA = DistanceFormula.EQUIRECTANGULAR;
    /**
     * Mean Earth radius in meters.
     */
    public static final double EARTH_MEAN_RADIUS_METERS = 6_371_009; // meters
    private static final double MAX_LAT = Math.toRadians(90d); // PI/2
    private static final double MAX_LON = Math.toRadians(180d); // PI
    private static final double MIN_LAT = Math.toRadians(-90d); // -PI/2
    private static final double MIN_LON = Math.toRadians(-180d); // -PI

    private static final long serialVersionUID = -8972065367390749356L;

    private final DistanceFormula df;
    private int hash;
    private final LatLng latlng;

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
    @Nonnull
    public List<GeoPosition> boundingBox(final double range) {
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

    @Override
    public double distanceTo(@Nonnull final GeoPosition other) {
        if (other instanceof LatLongPosition) {
            return distance(latlng, ((LatLongPosition) other).latlng, df);
        }
        return distance(latlng, new LatLng(other.getLatitude(), other.getLongitude()), df);
    }

    private GeoPosition ebeOperation(final BinaryOperator<Double> op, final GeoPosition other) {
        return new LatLongPosition(
                op.apply(getLatitude(), other.getLatitude()),
                op.apply(getLongitude(), other.getLongitude()));
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
    @Nonnull
    public double[] getCoordinates() {
        return new double[] { getLatitude(), getLongitude() };
    }

    @Override
    public double getCoordinate(final int dimension) {
        if (dimension == 0) {
            return getLatitude();
        }
        if (dimension == 1) {
            return getLongitude();
        }
        throw new IllegalArgumentException("Pass 1 for longitude or 0 for latitude. No other value accepted.");
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    /**
     * @return the latitude
     */
    @Override
    public double getLatitude() {
        return latlng.getLatitude();
    }

    /**
     * @return the longitude
     */
    @Override
    public double getLongitude() {
        return latlng.getLongitude();
    }

    @Override
    public double getX() {
        return getLongitude();
    }

    @Override
    public double getY() {
        return getLatitude();
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Hashes.hash32(getLatitude(), getLongitude());
        }
        return hash;
    }

    @Override
    public GeoPosition minus(@Nonnull final GeoPosition other) {
        return ebeOperation((self, o) -> self - o, other);
    }

    @Nonnull
    @Override
    public GeoPosition minus(@Nonnull final double[] other) {
        return new LatLongPosition(getLatitude() - other[0], getLongitude() - other[1]);
    }

    @Nonnull
    @Override
    public GeoPosition plus(@Nonnull final double[] other) {
        return new LatLongPosition(getLatitude() + other[0], getLongitude() + other[1]);
    }

    @Override
    public GeoPosition plus(@Nonnull final GeoPosition other) {
        return ebeOperation(Double::sum, other);
    }

    @Override
    public String toString() {
        return latlng.toString();
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

}
