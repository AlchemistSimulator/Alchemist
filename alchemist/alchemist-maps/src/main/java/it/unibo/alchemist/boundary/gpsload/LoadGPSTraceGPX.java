package it.unibo.alchemist.boundary.gpsload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;
import at.jku.traces.json.GPSPointImpl;
import at.jku.traces.json.UserTrace;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/*
 * come gestire l'ID
 * tempo oggetto di tipo it.unibo.alchemist.model.interfacef.Time
 * */
/**
 * 
 */
public class LoadGPSTraceGPX implements LoadGPSTraceStrategy {

    private static final int ID = 0; 

    @Override
    public GPSTrace readTrace(final InputStream pathFile) throws IOException {
        Objects.requireNonNull(pathFile, "input stream is null!!!");
        final GPX gpxFile = GPX.read(pathFile);
        return this.getTrace(gpxFile.tracks().findFirst().orElse(null));
    }

    @Override
    public GPSTrace readTrace(final InputStream pathFile, final String traceName) throws IOException {
        Objects.requireNonNull(pathFile, "input stream is null!!!");
        final GPX gpxFile = GPX.read(pathFile);
        return this.getTrace(gpxFile.tracks()
                                    .filter(track -> track.getName().isPresent() && track.getName().get().equals(traceName))
                                    .findFirst()
                                    .orElse(null));
    }

    private GPSTrace getTrace(final Track track) {
        /*verifico che la traccia gps esista*/
        Objects.requireNonNull(track, "request GPS track not found");
        final List<GPSPoint> trace = new LinkedList<>();
        /*estraggo la lista dei punti gps della traccia*/
        track.segments()
             .forEach(segment -> trace.addAll(segment.points()
                                                     .map(gpxPoint -> {
                                                         final double lat = gpxPoint.getLatitude().doubleValue();
                                                         final double longitude = gpxPoint.getLongitude().doubleValue();
                                                         final ZonedDateTime time;
                                                         if(gpxPoint.getTime().isPresent()) {
                                                             time = gpxPoint.getTime().get();
                                                         } else {
                                                             throw new IllegalArgumentException();
                                                         }
                                                         return new GPSPointImpl(lat,longitude, time.toInstant().toEpochMilli());
                                                                 
                                                     })
                                                     .collect(Collectors.toList())));
        /*creo il tracciato e normailizzo i tempi in base a quello di partenza*/
        final GPSTrace gpstrace = new UserTrace(ID, trace);
        gpstrace.normalizeTimes(gpstrace.getStartTime());
        return gpstrace;
    }
}
