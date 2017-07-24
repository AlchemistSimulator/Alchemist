package it.unibo.alchemist.boundary.gpsload;

import java.io.FileNotFoundException;
import java.io.IOException;

import gnu.trove.map.TIntObjectMap;

/**
 * strategy to define how to map node->track. 
 */
public interface LoadGPSMappingStrategy {

    /**
     * 
     * @param resource path of file with mapping
     * @return mapping <nodeId, fileTrace> 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    TIntObjectMap<MappingTrace> loadMapping(String resource) throws FileNotFoundException, IOException;
}
