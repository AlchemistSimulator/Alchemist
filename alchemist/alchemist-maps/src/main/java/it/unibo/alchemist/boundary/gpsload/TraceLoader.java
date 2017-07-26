package it.unibo.alchemist.boundary.gpsload;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import gnu.trove.map.TIntObjectMap;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class TraceLoader {

    private final TIntObjectMap<GPSTrace> mappingTrace;
    private final String directoryPath;

    /**
     * 
     * @param directoryPath directory with all file trace and mapping node-trace
     * @param readStrategy strategy to define how read gps trace and associate to the node
     * @param timeStartegy strategy to define how normalize time of all gps trace
     * @throws FileNotFoundException if a file is not found
     * @throws IOException error while reading a file
     */
    public TraceLoader(final String directoryPath,
            final LoadGPSTraceMappingStrategy readStrategy,
            final NormalizeTimeStrategy timeStartegy)
            throws FileNotFoundException, IOException {

        Objects.requireNonNull(readStrategy, "define a strategy for load traces");
        Objects.requireNonNull(timeStartegy, "define a strategy to normalize time");
        this.directoryPath = directoryPath;
        InputStream resource = toInputStream("");
        Objects.requireNonNull(resource, "the resource path for the directory don't exist");

        /* 
         * check if directoryPath is a directory
         */
        BufferedReader in = new BufferedReader(new InputStreamReader(resource));
        final boolean isDirectory = in.lines().allMatch(line -> resourceExists(line));
        /*
         * stream consumed, close it 
         */
        in.close();
        resource.close();
        if (!isDirectory) {
            throw new IllegalArgumentException("the directory path isn't a directory");
        }
        /*
         *  re-open stream to verify if the resource is not empty 
         */
        resource = toInputStream("");
        in = new BufferedReader(new InputStreamReader(resource));
        final boolean isEmpty = !in.lines().findAny().isPresent();
        in.close();
        resource.close();
        if (isEmpty) {
            throw new IllegalArgumentException("the directory path is empty"); 
        }
        /*
         * load GPSTrace mapped for idNode
         */
        this.mappingTrace = timeStartegy.normalizeTime(readStrategy.getGPSTraceMapping(directoryPath));
    }

    private InputStream toInputStream(final String resource) {
        Objects.requireNonNull(resource, "non-existing resource " + resource);
        return TraceLoader.class.getResourceAsStream(this.directoryPath + "/" + resource);
    }

    private boolean resourceExists(final String resource) {
        return TraceLoader.class.getResource(this.directoryPath + "/" + resource) != null;
    }

    /**
     * 
     * @param nodeId Node you want the trace
     * @return the gps trace of the node
     */
    public GPSTrace getGPSTrace(final int nodeId) {
        return this.mappingTrace.get(nodeId);
    }

}
