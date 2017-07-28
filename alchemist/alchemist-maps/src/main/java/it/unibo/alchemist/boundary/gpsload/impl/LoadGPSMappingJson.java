package it.unibo.alchemist.boundary.gpsload.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.boundary.gpsload.api.NodeToTraceMapper;
import it.unibo.alchemist.boundary.gpsload.api.TraceRef;

/**
 * 
 */
public class LoadGPSMappingJson implements NodeToTraceMapper {

    private static final String JSON_FILE_NAME = "/mapping-config.json";
    private static final Gson GSON = new GsonBuilder().create();


    @Override
    public TIntObjectMap<TraceRef> loadMapping(final String path) throws FileNotFoundException, IOException {
        final String configPath = path + JSON_FILE_NAME;
        final InputStream resource = LoadGPSMappingJson.class.getResourceAsStream(configPath);
        if (resource == null) {
            throw new FileNotFoundException("Not a reference to a valid descriptor: " + configPath);
        }
        final TIntObjectMap<TraceRef> mapping = new TIntObjectHashMap<>();
        try (InputStreamReader isreader = new InputStreamReader(resource)) {
            final Collection<?> mappings = GSON.fromJson(isreader, Collection.class);
            for (final Object traceMapObj: mappings) {
                if (traceMapObj instanceof Map) {
                    final Map<?, ?> traceMap = (Map<?, ?>) traceMapObj;
                    final Object nodeIdObj = traceMap.get("idNode");
                    if (nodeIdObj instanceof Number) {
                        final String pathFile = Optional.ofNullable(traceMap.get("pathFile"))
                                .map(Object::toString)
                                .orElseThrow(() -> new MalformedJsonException("pathFile can't be null in " + traceMapObj));
                        final Optional<String> traceName = Optional.ofNullable(traceMap.get("traceName")).map(Object::toString);
                        if (mapping.put(((Number) nodeIdObj).intValue(), new TraceRef(pathFile, traceName)) != null) {
                            throw new MalformedJsonException("Invalid trace descriptor, id " + nodeIdObj + " appears multiple times.");
                        }
                    } else {
                        throw new MalformedJsonException("Non-integer id: " + nodeIdObj + " at: " + traceMap);
                    }
                } else {
                    throw new MalformedJsonException("Invalid descriptor, not a JSON Object: " + traceMapObj);
                }
            }
        }
        return mapping;
    }
}
