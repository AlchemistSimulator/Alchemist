package it.unibo.alchemist.boundary.gpsload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import gnu.trove.map.TIntObjectMap;

/**
 * 
 */
public interface LoadGPSMappingStrategy {

    /**
     * 
     * @param resource path of file with mapping
     * @return mapping <nodeId, fileTrace> 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    TIntObjectMap<MappingTrace> loadMapping(final String resource) throws FileNotFoundException, IOException;
}
