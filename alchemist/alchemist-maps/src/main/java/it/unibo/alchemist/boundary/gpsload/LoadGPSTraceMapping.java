package it.unibo.alchemist.boundary.gpsload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.model.interfaces.GPSTrace;

public class LoadGPSTraceMapping implements LoadGPSTraceMappingStrategy {

    private TIntObjectMap<MappingTrace> mappingPath;
    private final LoadGPSMappingStrategy readMapping;
    private final LoadGPSTraceStrategy readTrace;
    private String directoryPath;

    public LoadGPSTraceMapping(final LoadGPSMappingStrategy readMapping, final LoadGPSTraceStrategy readTrace) {
        this.readMapping = Objects.requireNonNull(readMapping, "define a strategy for load mapping configuration");
        this.readTrace = Objects.requireNonNull(readTrace, "define a strategy for load traces");
    }


    @Override
    public TIntObjectMap<GPSTrace> getGPSTraceMapping(final String directoryPath) throws FileNotFoundException, IOException {
        this.directoryPath = directoryPath;
        mappingPath = readMapping.loadMapping(directoryPath);
        /*read GPSTrace of every node*/
        final TIntObjectMap<GPSTrace> mappingTrace = new TIntObjectHashMap<>();
        for (final int key : mappingPath.keys()) {
            mappingTrace.put(key, this.loadGPSTrace(key));
        }
        return null;
    }

    private GPSTrace loadGPSTrace(final int idNode) throws IOException {
        final MappingTrace map = Objects.requireNonNull(mappingPath.get(idNode), "the node has no trace mapped");
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
