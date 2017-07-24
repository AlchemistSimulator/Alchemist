package it.unibo.alchemist.boundary.gpsload;

import java.io.FileNotFoundException;
import java.io.IOException;

import gnu.trove.map.TIntObjectMap;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * strategy to define how map node->GPSTrace from a directory path.
 */
public interface LoadGPSTraceMappingStrategy {

    /**
     * 
     * @param directoryPath that contains mapping file and file with track
     * @return map node->GPSTrace
     * @throws FileNotFoundException if at least a file not found
     * @throws IOException error during read operation
     */
    TIntObjectMap<GPSTrace> getGPSTraceMapping(String directoryPath) throws FileNotFoundException, IOException;
}
