package it.unibo.alchemist.boundary.gpsload.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;

import com.google.common.collect.ImmutableSet;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import it.unibo.alchemist.boundary.gpsload.api.GPSFileLoader;
import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.routes.GPSTraceImpl;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * Class that reads GPS tracks from gpx files. 
 */
public class GPXLoader implements GPSFileLoader {

    private static final ImmutableSet<String> EXTENSION = ImmutableSet.of("gpx");

    @Override
    public List<GPSTrace> readTrace(final URL url) throws IOException {

        final InputStream stream = url.openStream();
        final List<GPSTrace> ret = getGPX(stream).tracks().map(this::getTrace).collect(Collectors.toList()); 
        stream.close();
        return ret;
    }

    @Override
    public Collection<String> supportedExtensions() {
        return EXTENSION;
    }

    private GPX getGPX(final InputStream stream) throws FileFormatException {
        Objects.requireNonNull(stream, "Input stream can't be null");
        try {
            return GPX.read(stream);
        } catch (IOException e) {
            throw new FileFormatException("the gpx file can't be read...sure is a gpx file valid?");
        }
    }

    private GPSTrace getTrace(final Track track) {
        /*
         * check if track exist
         */
        Objects.requireNonNull(track, "request GPS track not found");
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
         * Points without time stamp
         */
        if (!track.segments()
                .map(TrackSegment::getPoints)
                .flatMap(List::stream)
                .map(WayPoint::getTime)
                .allMatch(Optional::isPresent)) {
            throw new IllegalStateException("Track " + track + " contains at least a waypoint without timestamp");
        }
        /*
         * Converts the Track points to Alchemist GPSPoints
         */
        final List<GPSPoint> points = track.segments()
            .flatMap(TrackSegment::points)
            .map(wp -> new GPSPointImpl(wp.getLatitude().doubleValue(),
                        wp.getLongitude().doubleValue(),
                        new DoubleTime(wp.getTime().get().toInstant().toEpochMilli())))
            .collect(Collectors.toList());
        return new GPSTraceImpl(points);
    }
}
