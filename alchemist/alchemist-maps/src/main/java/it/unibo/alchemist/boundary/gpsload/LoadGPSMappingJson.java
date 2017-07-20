package it.unibo.alchemist.boundary.gpsload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import com.google.gson.stream.JsonReader;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * 
 */
public class LoadGPSMappingJson implements LoadGPSMappingStrategy {

    private static final String JSON_FILE_NAME = "/mapping-config.json";
    private TIntObjectMap<MappingTrace> mapping;

    
    @Override
    public TIntObjectMap<MappingTrace> loadMapping(final String pathFile) throws FileNotFoundException, IOException {
        this.mapping = new TIntObjectHashMap<>();     
        this.loadJson(pathFile + JSON_FILE_NAME);
        return this.mapping;
    }

    private void loadJson(final String path) throws FileNotFoundException, IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(LoadGPSMappingJson.class.getResourceAsStream(path)))) {
            reader.beginArray();
            while (reader.hasNext()) {
                final MappingTrace map = this.readMapping(reader);
                this.mapping.put(map.getIdNode(), map);
            }
            reader.endArray();
        }
    }

    private MappingTrace readMapping(final JsonReader reader) throws IOException {
        Integer idNode = null;
        String pathFile = null;
        String tarceName = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String nameField = reader.nextName();
            if (nameField.equals("idNode")) {
                idNode = reader.nextInt();
            } else if (nameField.equals("pathFile")) {
                pathFile = reader.nextString();
            } else if (nameField.equals("tarceName")) {
                tarceName = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new MappingTrace(Objects.requireNonNull(idNode, "node id mustn't is null"), 
                                Objects.requireNonNull(pathFile, "file path mustn't is null"),
                                tarceName);

    }

}
