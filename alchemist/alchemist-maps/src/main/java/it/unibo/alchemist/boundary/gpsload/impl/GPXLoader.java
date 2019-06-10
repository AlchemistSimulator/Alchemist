/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gpsload.impl;

import com.google.common.collect.ImmutableSet;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import it.unibo.alchemist.boundary.gpsload.api.GPSFileLoader;
import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.routes.GPSTraceImpl;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Class that reads GPS tracks from gpx files. 
 */
public final class GPXLoader implements GPSFileLoader {

    private static final ImmutableSet<String> EXTENSION = ImmutableSet.of("gpx");

    @Override
    public List<GPSTrace> readTrace(final URL url) throws IOException {
        final InputStream stream = url.openStream();
        final List<GPSTrace> ret = getGPX(requireNonNull(stream, "Input stream can't be null"))
            .tracks()
            .map(track -> getTrace(requireNonNull(track, "request GPS track not found")))
            .collect(Collectors.toList());
        stream.close();
        return ret;
    }

    @Override
    public Collection<String> supportedExtensions() {
        return EXTENSION;
    }

    private GPX getGPX(final InputStream stream) throws FileFormatException {
        try {
            return GPX.read(stream);
        } catch (IOException e) {
            final FileFormatException realException = new FileFormatException(
                    "Cannot read the GPX content. Please make sure it is a valid GPX.");
            realException.initCause(e);
            throw realException;
        }
    }

    private GPSTrace getTrace(final Track track) {
        /*
         * No segments
         */
        if (track.getSegments().isEmpty()) {
            throw new IllegalStateException("Track " + track + " contains no segment");
        }
        /*
         * Empty segments
         */
        if (track.segments()
                .map(TrackSegment::getPoints)
                .mapToInt(List::size)
                .anyMatch(s -> s <= 0)) {
            throw new IllegalStateException("Track " + track + " contains at least a segment with no points");
        }
        /*
         * Converts the Track points to Alchemist GPSPoints
         */
        final List<GPSPoint> points = track.segments()
            .flatMap(TrackSegment::points)
            .map(wp -> new GPSPointImpl(
                    wp.getLatitude().doubleValue(),
                    wp.getLongitude().doubleValue(),
                    new DoubleTime(wp.getTime()
                        /*
                         * Points without time stamp
                         */
                        .orElseThrow(() -> new IllegalStateException("Track " + track + " contains at least a waypoint without timestamp"))
                        .toInstant().toEpochMilli() / 1000.0)))
            .collect(Collectors.toList());
        return new GPSTraceImpl(points);
    }
}
