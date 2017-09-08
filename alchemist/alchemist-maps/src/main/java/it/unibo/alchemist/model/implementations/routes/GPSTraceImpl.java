/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.routes;

import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.utils.MapUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.math3.util.Pair;

import com.google.common.collect.ImmutableList;

/**
 */
public class GPSTraceImpl implements GPSTrace {

    private static final long serialVersionUID = 1L;
    private final ImmutableList<GPSPoint> trace;
    private double len = Double.NaN;

    /**
     * 
     * @param tr
     *            GPS points
     */
    public GPSTraceImpl(final GPSPoint... tr) {
        this(Arrays.asList(tr));
    }

    /**
     * @param tr
     *            GPS points
     */
    public GPSTraceImpl(final List<GPSPoint> tr) {
        trace = ImmutableList.sortedCopyOf(tr);
    }

    @Override
    public GPSTrace startAt(final Time time) {
        return new GPSTraceImpl(trace.stream()
            .filter(pt -> pt.getTime().toDouble() >= time.toDouble())
            .map(p -> new GPSPointImpl(p.getLatitude(), p.getLongitude(), p.getTime().subtract(time)))
            .toArray(GPSPoint[]::new));
    }

    @Override
    public GPSPoint getNextPosition(final Time time) {
        checkNotEmpty();
        return searchPoint(time).getSecond();
    }

    private void checkNotEmpty() {
        if (trace.isEmpty()) {
            throw new IllegalStateException("The trace has no points.");
        }
    }

    @Override
    public GPSPoint getPreviousPosition(final Time time) {
        checkNotEmpty();
        return searchPoint(time).getFirst();
    }

    @Override
    public Time getStartTime() {
        checkNotEmpty();
        return trace.get(0).getTime();
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

    @Override
    public double length() {
        if (Double.isNaN(len)) {
            len = 0;
            for (int i = 0; i < trace.size() - 1; i++) {
                len += trace.get(i).getDistanceTo(trace.get(i + 1));
            }
        }
        return len;
    }

    private Pair<GPSPoint, GPSPoint> searchPoint(final Time time) {
        if (trace.size() < 2 || time.toDouble() < trace.get(0).getTime().toDouble()) {
            return new Pair<>(trace.get(0), trace.get(0));
        }
        if (trace.size() < 3) {
            return new Pair<>(trace.get(0), trace.get(1));
        }
        if (time.toDouble() > trace.get(trace.size() - 1).getTime().toDouble()) {
            return new Pair<>(trace.get(trace.size() - 1), trace.get(trace.size() - 1));
        }
        int low = 0;
        int high = trace.size() - 1;
        for (int i = trace.size() / 2; high - low > 1; i = low + (high - low) / 2) {
            if (trace.get(i).getTime().toDouble() < time.toDouble()) {
                low = i;
            } else {
                high = i;
            }
        }
        return new Pair<>(trace.get(low), trace.get(high));
    }

    @Override
    public int size() {
        return trace.size();
    }

    @Override
    public String toString() {
        return trace.toString();
    }

    @Override
    public GPSPoint getPoint(final int step) {
        if (step < size()) {
            return trace.get(step);
        }
        throw new IllegalArgumentException(step + " is not a valid point number for this route (lenght " + size() + ')');
    }

    @Override
    public List<GPSPoint> getPoints() {
        return trace;
    }

    @Override
    public Stream<GPSPoint> stream() {
        return trace.stream();
    }

    @Override
    public double getTripTime() {
        return trace.get(trace.size() - 1).getTime().toDouble() - trace.get(0).getTime().toDouble();
    }

    @Override
    public Iterator<GPSPoint> iterator() {
        return getPoints().iterator();
    }

    @Override
    public GPSPoint getInitialPosition() {
        return trace.get(0);
    }

    @Override
    public GPSPoint getFinalPosition() {
        return trace.get(trace.size() - 1);
    }

    @Override
    public Time getFinalTime() {
        return trace.get(trace.size() - 1).getTime();
    }

}
