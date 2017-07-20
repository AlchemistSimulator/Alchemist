package it.unibo.alchemist.boundary.gpsload;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class TraceLoader {

    private final TIntObjectMap<MappingTrace> mappingPath;
    private final TIntObjectMap<GPSTrace> mappingTrace;
    private final LoadGPSTraceStrategy readTrace;
    private final LoadGPSMappingStrategy readMapping;

    /**
     * 
     * @param directoryPath
     * @param readTrace
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TraceLoader(final String directoryPath, final LoadGPSTraceStrategy readTrace,
            final LoadGPSMappingStrategy readMapping)
            throws IllegalArgumentException, FileNotFoundException, FileFormatException, IOException {

        this.readMapping = Objects.requireNonNull(readMapping, "define a strategy for load mapping");
        this.readTrace = Objects.requireNonNull(readTrace, "define a strategy for load traces");
        
        final InputStream resource = this.getInputStream(directoryPath);
        Objects.requireNonNull(resource, "the resource path for the directory don't exist");
        /* check if the resource is a directory or a file*/
        String line = null;
        boolean isDirectory = true;
        boolean isEmpty = true;
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(resource))) {
            while (isDirectory && (line = in.readLine()) != null) {
                isEmpty = false;
                if (this.getInputStream(line) == null) {
                    isDirectory = false;
                }
            }
        } catch (IOException e){
            throw new IOException("Error reading directory content");
        } finally {
            try {
                resource.close();
            } catch (IOException e) {
                throw new IOException("Error while closing the input stream of the directory");
            }
        }
        
        /* check if directoryPath is a directory not empty*/
        if (isDirectory && !isEmpty) {
            this.mappingPath = this.readMapping.loadMapping(directoryPath);
        } else {
            throw new IllegalArgumentException("the directory path isn't a directory or is empty");
        }
        /*read GPSTrace of every node*/
        this.mappingTrace = new TIntObjectHashMap<>();
        for (int key : this.mappingPath.keys()) {
            this.mappingTrace.put(key, this.loadGPSTrace(key));
        }
    }

    private GPSTrace loadGPSTrace(int idNode) throws IOException {
        final MappingTrace map = this.mappingPath.get(idNode);
        Objects.requireNonNull(map, "the node hasn't a trace mapped");
        if (map.getTraceName() == null) {
            return this.readTrace.readTrace(this.getInputStream(map.getPathFile()));
        } else {
            return this.readTrace.readTrace(this.getInputStream(map.getPathFile()), map.getTraceName());
        }
    }
    
    private InputStream getInputStream(String resouce) {
        Objects.requireNonNull(resouce, "resouce not passed");
        return TraceLoader.class.getResourceAsStream(resouce);
    }

    /**
     * 
     * @param node
     * @return
     * @throws FileNotFoundException
     * @throws FileFormatException
     */
    public GPSTrace getGPSTrace(final int nodeId) {
        return this.mappingTrace.get(nodeId);
    }

}
