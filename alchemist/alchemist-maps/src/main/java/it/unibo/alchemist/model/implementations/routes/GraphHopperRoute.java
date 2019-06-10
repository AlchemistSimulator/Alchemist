/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;

import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.TimedRoute;

/**
 */
public final class GraphHopperRoute implements TimedRoute<GeoPosition> {

    private static final long serialVersionUID = -1455332156736222268L;
    private final int numPoints;
    private final double distance, time;
    private final List<GeoPosition> points;

    /**
     * @param response
     *            the response to use
     */
    public GraphHopperRoute(final GHResponse response) {
        final List<Throwable> errs = response.getErrors();
        if (errs.isEmpty()) {
            final PathWrapper resp = response.getBest();
            time = resp.getTime() / 1000d;
            distance = resp.getDistance();
            final PointList pts = resp.getPoints();
            numPoints = pts.getSize();
            final List<GeoPosition> temp = new ArrayList<>(numPoints);
            for (int i = 0; i < pts.getSize(); i++) {
                temp.add(new LatLongPosition(pts.getLatitude(i), pts.getLongitude(i)));
            }
            points = Collections.unmodifiableList(temp);
        } else {
            final String msg = errs.stream().map(Throwable::getMessage).collect(Collectors.joining("\n"));
            throw new IllegalArgumentException(msg, errs.get(0));
        }
    }

    @Override
    public double length() {
        return distance;
    }

    @Override
    public GeoPosition getPoint(final int step) {
        return points.get(step);
    }

    @Override
    public List<GeoPosition> getPoints() {
        return points;
    }

    @Override
    public double getTripTime() {
        return time;
    }

    @Override
    public Iterator<GeoPosition> iterator() {
        return points.iterator();
    }

    @Override
    public Stream<GeoPosition> stream() {
        return points.stream();
    }

    @Override
    public int size() {
        return numPoints;
    }

}
