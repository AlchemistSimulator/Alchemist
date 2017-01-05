/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package at.jku.traces.json;

import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.utils.MapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.Pair;

/**
 */
public class UserTrace implements GPSTrace {

    /**
     * If an error occurs, this object is returned.
     */
    public static final GPSPoint FAILURE = new GPSPointImpl(Double.NaN, Double.NaN, Double.NaN);
    private static final long serialVersionUID = -6060550940453129358L;
    private int mi;
    private final GPSPoint[] trace;
    private double len = Double.NaN;

    /**
     * 
     * @param id
     *            user id
     * @param tr
     *            GPS points
     */
    public UserTrace(final int id, final GPSPoint[] tr) {
        mi = id;
        trace = Arrays.copyOf(tr, tr.length);
    }

    /**
     * @param id
     *            user id
     * @param tr
     *            GPS points
     */
    public UserTrace(final int id, final List<GPSPoint> tr) {
        mi = id;
        trace = tr.toArray(new GPSPoint[tr.size()]);
    }

    @Override
    public GPSTrace filter(final double time) {
        final List<GPSPoint> pts = new ArrayList<>(trace.length);
        for (final GPSPoint tr : trace) {
            if (tr.getTime() >= time) {
                pts.add(new GPSPointImpl(tr.getLatitude(), tr.getLongitude(), tr.getTime() - time));
            }
        }
        return new UserTrace(getId(), pts);
    }

    @Override
    public int getId() {
        return mi;
    }

    @Override
    public GPSPoint getNextPosition(final double time) {
        if (trace.length == 0) {
            return FAILURE;
        }
        return searchPoint(time).getSecond();
    }

    @Override
    public GPSPoint getPreviousPosition(final double time) {
        if (trace.length == 0) {
            return FAILURE;
        }
        return searchPoint(time).getFirst();
    }

    @Override
    public double getStartTime() {
        sort();
        if (trace.length > 0) {
            return trace[0].getTime();
        }
        return Double.NaN;
    }

    @Override
    public GPSPoint interpolate(final double time) {
        final Pair<GPSPoint, GPSPoint> coords = searchPoint(time);
        final GPSPoint prev = coords.getFirst();
        final GPSPoint next = coords.getSecond();
        final double tdtime = next.getTime() - prev.getTime();
        if (tdtime == 0) {
            return next;
        }
        final double ratio = (time - prev.getTime()) / tdtime;
        final Position start = prev.toPosition();
        final Position end = next.toPosition();
        final double dist = MapUtils.getDistance(start, end);
        return new GPSPointImpl(MapUtils.getDestinationLocation(start, end, dist * ratio), time);
    }

    @Override
    public double length() {
        if (Double.isNaN(len)) {
            len = 0;
            for (int i = 0; i < trace.length - 1; i++) {
                len += MapUtils.getDistance(trace[i].getLatitude(), trace[i].getLongitude(), trace[i + 1].getLatitude(), trace[i + 1].getLongitude());
            }
        }
        return 0;
    }

    @Override
    public void normalizeTimes(final double initialTime) {
        for (final GPSPoint p : trace) {
            p.setTime(p.getTime() - initialTime);
        }
    }

    private Pair<GPSPoint, GPSPoint> searchPoint(final double time) {
        if (trace.length < 2 || time < trace[0].getTime()) {
            return new Pair<>(trace[0], trace[0]);
        }
        if (trace.length < 3) {
            return new Pair<>(trace[0], trace[1]);
        }
        if (time > trace[trace.length - 1].getTime()) {
            return new Pair<>(trace[trace.length - 1], trace[trace.length - 1]);
        }
        int low = 0;
        int high = trace.length - 1;
        for (int i = trace.length / 2; high - low > 1; i = low + (high - low) / 2) {
            if (trace[i].getTime() < time) {
                low = i;
            } else {
                high = i;
            }
        }
        return new Pair<>(trace[low], trace[high]);
    }

    @Override
    public void setId(final int i) {
        mi = i;
    }

    @Override
    public int size() {
        return trace.length;
    }

    @Override
    public void sort() {
        Arrays.sort(trace);
    }

    @Override
    public String toString() {
        return mi + ": " + Arrays.toString(trace);
    }

}
