package it.unibo.alchemist.boundary.gpsload.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.boundary.gpsload.api.NodeToTraceMapper;
import it.unibo.alchemist.boundary.gpsload.api.TraceRef;
/**
 * 
 */
public class LoadGPSMapping implements NodeToTraceMapper {

    private final int numberNode;
    private final TIntObjectMap<TraceRef> mapping;

    /**
     * 
     * @param numberNode number of node to mapped
     */
    public LoadGPSMapping(final int numberNode) {
        this.numberNode = numberNode;
        mapping = new TIntObjectHashMap<>(numberNode);
    }

    @Override
    public TIntObjectMap<TraceRef> loadMapping(final String resource) throws FileNotFoundException, IOException {
        List<String> filesName;
        /*
         * load List<String> of file name
         */
        try (BufferedReader in = new BufferedReader(new InputStreamReader(toInputStream(resource)))) {
            filesName = in.lines()
                    .filter(line -> resourceExists(resource + "/" + line))
                    .collect(Collectors.toList());
        }
        /*
         * mapping
         */
        for (int i = 0; i < numberNode; i++) {
            mapping.put(i, new TraceRef(filesName.get(i % filesName.size())));
        }
        return mapping;
    }

    private InputStream toInputStream(final String resource) {
        Objects.requireNonNull(resource, "non-existing resource " + resource);
        return LoadGPSMapping.class.getResourceAsStream(resource);
    }

    private boolean resourceExists(final String resource) {
        return LoadGPSMapping.class.getResource(resource) != null;
    }

}
