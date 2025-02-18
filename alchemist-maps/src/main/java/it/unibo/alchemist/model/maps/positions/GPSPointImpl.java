/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.positions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.maps.GPSPoint;
import org.danilopianini.util.Hashes;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.util.List;

/**
 */
public final class GPSPointImpl implements GPSPoint {

    @Serial
    private static final long serialVersionUID = 1L;
    private final LatLongPosition repr;
    private final Time time;

    /**
     * @param latitude
     *            latitude
     * @param longitude
     *            longitude
     * @param time
     *            time
     */
    public GPSPointImpl(final double latitude, final double longitude, final Time time) {
        this(new LatLongPosition(latitude, longitude), time);
    }

    /**
     * @param latLongPosition
     *            latitude and longitude
     * @param time
     *            time
     */
    public GPSPointImpl(final LatLongPosition latLongPosition, final Time time) {
        this.repr = latLongPosition;
        this.time = time;
    }

    @Override
    public GPSPointImpl addTime(final Time shift) {
        return new GPSPointImpl(repr, this.time.plus(shift));
    }

    @Override
    @Nonnull
    public List<GeoPosition> boundingBox(final double range) {
        return repr.boundingBox(range);
    }

    @Override
    public int compareTo(final GPSPoint p) {
        return (int) Math.signum(time.toDouble() - p.getTime().toDouble());
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof final GPSPointImpl pt) {
            return pt.getTime().equals(time) && repr.equals(pt.repr);
        }
        return false;
    }

    @Override
    @Nonnull
    public double[] getCoordinates() {
        return repr.getCoordinates();
    }

    @Override
    public double getCoordinate(final int dimension) {
        return repr.getCoordinate(dimension);
    }

    @Override
    public GeoPosition plus(final GeoPosition other) {
        return repr.plus(other);
    }

    @Nonnull
    @Override
    public GeoPosition plus(@Nonnull final double[] other) {
        return repr.plus(other);
    }

    @Override
    public GeoPosition minus(final GeoPosition other) {
        return repr.minus(other);
    }

    @Nonnull
    @Override
    public GeoPosition minus(@Nonnull final double[] other) {
        return repr.minus(other);
    }

    @Override
    public int getDimensions() {
        return repr.getDimensions();
    }

    @Override
    public double distanceTo(@Nonnull final GeoPosition other) {
        if (other instanceof GPSPointImpl) {
            return repr.distanceTo(((GPSPointImpl) other).repr);
        }
        return repr.distanceTo(other);
    }

    @Override
    public double getLatitude() {
        return repr.getLatitude();
    }

    @Override
    public double getLongitude() {
        return repr.getLongitude();
    }

    @Override
    public Time getTime() {
        return time;
    }

    @Override
    public double getX() {
        return repr.getX();
    }

    @Override
    public double getY() {
        return repr.getY();
    }

    @Override
    public int hashCode() {
        return Hashes.hash32(repr, time);
    }

    @Override
    public GPSPointImpl subtractTime(final Time t) {
        return new GPSPointImpl(repr, this.time.minus(t));
    }

    @Override
    public String toString() {
        return "[" + repr.getLatitude() + "," + repr.getLongitude() + "]@" + time;
    }
}
