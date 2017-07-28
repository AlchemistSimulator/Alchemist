package it.unibo.alchemist.boundary.gpsload.api;

import java.io.IOException;
import java.io.InputStream;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * Strategy to read GPSTrace from file.
 */
public interface GPSFileLoader {

    /**
     * 
     * @param stream file with the trace request
     * @return GPSTrace readed
     * @throws FileFormatException file format not valid
     * @throws IOException 
     */
    GPSTrace readTrace(InputStream stream) throws FileFormatException, IOException;
    /**
     * 
     * @param stream file with the trace request
     * @param traceName name of GPSTrace request
     * @return GPSTrace readed
     * @throws FileFormatException file format not valid
     * @throws IOException 
     */
    GPSTrace readTrace(InputStream stream, String traceName) throws FileFormatException, IOException;

}
