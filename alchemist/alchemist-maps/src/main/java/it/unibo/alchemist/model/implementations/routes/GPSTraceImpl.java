/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.routes;

import java.util.List;

import org.apache.commons.math3.util.Pair;

import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.utils.MapUtils;

/**
 */
public class GPSTraceImpl extends PolygonalChain<GPSPoint> implements GPSTrace {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param tr
     *            GPS points
     */
    public GPSTraceImpl(final GPSPoint... tr) {
       super(tr);
    }

    /**
     * @param tr
     *            GPS points
     */
    public GPSTraceImpl(final List<GPSPoint> tr) {
        this(tr.stream().sorted().toArray(GPSPoint[]::new));
    }

    @Override
    public GPSTrace startAt(final Time time) {
        final GPSPoint[] filtered = stream()
            .filter(pt -> pt.getTime().toDouble() >= time.toDouble())
            .map(p -> new GPSPointImpl(p.getLatitude(), p.getLongitude(), p.getTime().subtract(time)))
            .toArray(GPSPoint[]::new);
        return new GPSTraceImpl(filtered.length == 0 ? new GPSPoint[] { getFinalPosition() } : filtered);
    }

    @Override
    public GPSPoint getNextPosition(final Time time) {
        return searchPoint(time).getSecond();
    }

    @Override
    public GPSPoint getPreviousPosition(final Time time) {
        return searchPoint(time).getFirst();
    }

    @Override
    public Time getStartTime() {
        return getPoints().get(0).getTime();
    }

    @Override
    public GPSPoint interpolate(final Time time) {
        final Pair<GPSPoint, GPSPoint> coords = searchPoint(time);
        final GPSPoint prev = coords.getFirst();
        final GPSPoint next = coords.getSecond();
        final double tdtime = next.getTime().toDouble() - prev.getTime().toDouble();
        if (tdtime == 0) {
            return next;
        }
        final double ratio = (time.toDouble() - prev.getTime().toDouble()) / tdtime;
        final double dist = MapUtils.getDistance(prev, next);
        return new GPSPointImpl(MapUtils.getDestinationLocation(prev, next, dist * ratio), time);
    }

    private Pair<GPSPoint, GPSPoint> searchPoint(final Time time) {
        if (size() < 2 || time.toDouble() < getPoint(0).getTime().toDouble()) {
            return new Pair<>(getPoint(0), getPoint(0));
        }
        if (size() < 3) {
            return new Pair<>(getPoint(0), getPoint(1));
        }
        if (time.toDouble() > getPoint(size() - 1).getTime().toDouble()) {
            return new Pair<>(getPoint(size() - 1), getPoint(size() - 1));
        }
        int low = 0;
        int high = size() - 1;
        for (int i = size() / 2; high - low > 1; i = low + (high - low) / 2) {
            if (getPoint(i).getTime().toDouble() < time.toDouble()) {
                low = i;
            } else {
                high = i;
            }
        }
        return new Pair<>(getPoint(low), getPoint(high));
    }

    @Override
    public double getTripTime() {
        return getPoint(size() - 1).getTime().toDouble() - getPoint(0).getTime().toDouble();
    }

    @Override
    public GPSPoint getInitialPosition() {
        return getPoint(0);
    }

    @Override
    public GPSPoint getFinalPosition() {
        return getPoint(size() - 1);
    }

    @Override
    public Time getFinalTime() {
        return getPoint(size() - 1).getTime();
    }

}
