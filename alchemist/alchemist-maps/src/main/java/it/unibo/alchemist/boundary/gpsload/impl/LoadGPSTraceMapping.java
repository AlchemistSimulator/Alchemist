package it.unibo.alchemist.boundary.gpsload.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.boundary.gpsload.api.GPSDataLoader;
import it.unibo.alchemist.boundary.gpsload.api.GPSFileLoader;
import it.unibo.alchemist.boundary.gpsload.api.NodeToTraceMapper;
import it.unibo.alchemist.boundary.gpsload.api.TraceRef;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class LoadGPSTraceMapping implements GPSDataLoader {

    private TIntObjectMap<TraceRef> mappingPath;
    private final NodeToTraceMapper mapper;
    private final GPSFileLoader readTrace;
    private String directoryPath;

    /**
     * 
     * @param readMapping strategy to load map node->track 
     * @param readTrace strategy to read the track from file
     */
    public LoadGPSTraceMapping(final NodeToTraceMapper readMapping, final GPSFileLoader readTrace) {
        this.mapper = Objects.requireNonNull(readMapping, "define a strategy for loading mapping configuration");
        this.readTrace = Objects.requireNonNull(readTrace, "define a strategy for loading traces");
    }


    @Override
    public TIntObjectMap<GPSTrace> getGPSTraceMapping(final String path) throws FileNotFoundException, IOException {
        this.directoryPath = path;
        mappingPath = mapper.loadMapping(path);
        /*read GPSTrace of every node*/
        final TIntObjectMap<GPSTrace> mappingTrace = new TIntObjectHashMap<>();
        for (final int key : mappingPath.keys()) {
            mappingTrace.put(key, this.loadGPSTrace(key));
        }
        return mappingTrace;
    }

    private GPSTrace loadGPSTrace(final int idNode) throws IOException {
        final TraceRef map = Objects.requireNonNull(mappingPath.get(idNode), "the node has no trace mapped");
        if (map.getTraceName().isPresent()) {
            return this.readTrace.readTrace(this.toInputStream(map.getPathFile()), map.getTraceName().get());
        } else {
            return this.readTrace.readTrace(this.toInputStream(map.getPathFile()));
        }
    }

    private InputStream toInputStream(final String resource) {
        Objects.requireNonNull(resource, "non-existing resource " + resource);
        return LoadGPSTraceMapping.class.getResourceAsStream(directoryPath + "/" + resource);
    }

}
