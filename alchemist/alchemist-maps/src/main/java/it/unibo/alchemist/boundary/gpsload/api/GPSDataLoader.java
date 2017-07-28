package it.unibo.alchemist.boundary.gpsload.api;

import java.io.FileNotFoundException;
import java.io.IOException;

import gnu.trove.map.TIntObjectMap;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * strategy to define how map node->GPSTrace from a directory path.
 */
@FunctionalInterface
public interface GPSDataLoader {

    /**
     * 
     * @param path that contains mapping file and file with track
     * @return map node->GPSTrace
     * @throws FileNotFoundException if at least a file not found
     * @throws IOException error during read operation
     */
    TIntObjectMap<GPSTrace> getGPSTraceMapping(String path) throws FileNotFoundException, IOException;
}
