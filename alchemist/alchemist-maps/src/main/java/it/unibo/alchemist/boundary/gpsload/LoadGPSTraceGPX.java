package it.unibo.alchemist.boundary.gpsload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import at.jku.traces.json.GPSTraceImpl;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class LoadGPSTraceGPX implements LoadGPSTraceStrategy {

    @Override
    public GPSTrace readTrace(final InputStream stream) throws IOException {
        return this.getTrace(getGPX(stream).tracks().findFirst().orElse(null));
    }

    @Override
    public GPSTrace readTrace(final InputStream stream, final String traceName) throws IOException {
        final GPX gpxFile = this.getGPX(stream);
        return this.getTrace(gpxFile.tracks()
            .filter(track -> track.getName().isPresent() && track.getName().get().equals(traceName))
            .findFirst()
            .orElse(null));
    }

    private GPX getGPX(final InputStream stream) throws IOException {
        Objects.requireNonNull(stream, "input stream is null!!!");
        try {
            return GPX.read(stream);
        } catch (IOException e) {
            throw new IOException("the gpx file can't be read...sure is a gpx file valid??");
        }
    }

    private GPSTrace getTrace(final Track track) throws IllegalStateException {
        /*verifico che la traccia gps esista*/
        Objects.requireNonNull(track, "request GPS track not found");
        final List<TrackSegment> segments = track.getSegments();
        /*
         * No segments
         */
        if (segments.isEmpty()) {
            throw new IllegalStateException("Track " + track + " contains no segment");
        }
        /*
         * Empty segments
         */
        if (track.segments()
                .map(TrackSegment::getPoints)
                .mapToInt(List::size)
                .anyMatch(s -> s < 0)) {
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
//        final List<GPSPoint> trace = new LinkedList<>();
//        final List<List<GPSPoint>> segments = new LinkedList<>();
//        /*estraggo la lista dei punti gps della traccia*/
//        track.segments()
//             .forEach(segment -> segments.add(segment.points()
//                 .map(gpxPoint -> {
//                     final double lat = gpxPoint.getLatitude().doubleValue();
//                     final double longitude = gpxPoint.getLongitude().doubleValue();
//                     final ZonedDateTime time;
//                     if (gpxPoint.getTime().isPresent()) {
//                         time = gpxPoint.getTime().get();
//                     } else {
//                         throw new IllegalArgumentException("point whit element time not present");
//                     }
//                     return new GPSPointImpl(lat, longitude, time.toInstant().toEpochMilli());                                                     })
//                 .collect(Collectors.toList())));
//        /*check if all segments have at last one point*/
//        if (segments.stream().allMatch(segment -> segment.size() > 0)) {
//            segments.stream().forEach(segment -> trace.addAll(segment));
//        } else {
//            throw new IllegalStateException("Trace contains segment with no points");
//        }
//        /*
//         * check if the trace is not empty
//         */
//        if (trace.isEmpty()) {
//            throw new IllegalStateException("Trace contains no points");
//        }
//        /*creo il tracciato e normailizzo i tempi in base a quello di partenza*/
//        final GPSTrace gpstrace = new UserTrace(ID, trace);
//        gpstrace.normalizeTimes(gpstrace.getStartTime());
//        return gpstrace;
    }
}
