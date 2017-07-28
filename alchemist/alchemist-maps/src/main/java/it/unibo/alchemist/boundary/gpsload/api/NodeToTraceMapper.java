package it.unibo.alchemist.boundary.gpsload.api;

import java.io.FileNotFoundException;
import java.io.IOException;

import gnu.trove.map.TIntObjectMap;

/**
 * strategy to define how to map node->track. 
 */
@FunctionalInterface
public interface NodeToTraceMapper {

    /**
     * 
     * @param resource path of file with mapping
     * @return mapping <nodeId, fileTrace> 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    TIntObjectMap<TraceRef> loadMapping(String resource) throws FileNotFoundException, IOException;
}
