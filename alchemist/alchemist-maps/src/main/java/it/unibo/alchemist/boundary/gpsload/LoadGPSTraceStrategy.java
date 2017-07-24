package it.unibo.alchemist.boundary.gpsload;

import java.io.IOException;
import java.io.InputStream;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * Strategy to read GPSTrace from file.
 */
public interface LoadGPSTraceStrategy {

    /**
     * 
     * @param stream file with the trace request
     * @return GPSTrace readed
     * @throws IllegalStateException if the trace or a segment's trace not contain GPSPoint
     * @throws FileFormatException
     * @throws IOException 
     */
    GPSTrace readTrace(InputStream stream) throws FileFormatException, IllegalStateException, IOException;
    /**
     * 
     * @param stream file with the trace request
     * @param traceName name of GPSTrace request
     * @return GPSTrace readed
     * @throws IllegalStateException if the trace or a segment's trace not contain GPSPoint
     * @throws FileFormatException
     * @throws IOException 
     */
    GPSTrace readTrace(InputStream stream, String traceName) throws FileFormatException, IllegalStateException, IOException;

}
