/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.positions;

import java.util.List;

import org.danilopianini.util.Hashes;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.Time;

/**
 */
public final class GPSPointImpl implements GPSPoint {

    private static final long serialVersionUID = -6060550940453129358L;
    private final LatLongPosition repr;
    private final Time t;

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
     * @param latlong
     *            latitude and longitude
     * @param time
     *            time
     */
    public GPSPointImpl(final LatLongPosition latlong, final Time time) {
        this.repr = latlong;
        this.t = time;
    }

    @Override
    public GeoPosition plus(final GeoPosition other) {
        return repr.plus(other);
    }

    @Override
    public GPSPointImpl addTime(final Time t) {
        return new GPSPointImpl(repr, this.t.plus(t));
    }

    @Override
    public List<LatLongPosition> boundingBox(final double range) {
        return repr.boundingBox(range);
    }

    @Override
    public int compareTo(final GPSPoint p) {
        return (int) Math.signum(t.toDouble() - p.getTime().toDouble());
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public boolean equals(final Object obj) {
        if (obj instanceof GPSPointImpl) {
            final GPSPointImpl pt = (GPSPointImpl) obj;
            return pt.getTime() == t && repr.equals(pt.repr);
        }
        return false;
    }

    @Override
    public double[] getCartesianCoordinates() {
        return repr.getCartesianCoordinates();
    }

    @Override
    public double getCoordinate(final int dim) {
        return repr.getCoordinate(dim);
    }

    @Override
    public int getDimensions() {
        return repr.getDimensions();
    }

    @Override
    public double getDistanceTo(final GeoPosition p) {
        if (p instanceof GPSPointImpl) {
            return repr.getDistanceTo(((GPSPointImpl) p).repr);
        }
        return repr.getDistanceTo(p);
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
        return t;
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
        return Hashes.hash32(repr, t);
    }

    @Override
    public GeoPosition minus(final GeoPosition other) {
        return repr.minus(other);
    }

    @Override
    public GPSPointImpl subtractTime(final Time t) {
        return new GPSPointImpl(repr, this.t.minus(t));
    }

    @Override
    public String toString() {
        return "[" + repr.getLatitude() + "," + repr.getLongitude() + "]@" + t;
    }
}
