package it.unibo.alchemist.boundary.gpsload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;

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
        final String configPath = pathFile + JSON_FILE_NAME;
        this.loadJson(configPath);
        return this.mapping;
    }

    private void loadJson(final String path) throws FileNotFoundException, IOException {
        final InputStream stream = LoadGPSMappingJson.class.getResourceAsStream(path);
        if (stream == null) {
            throw new FileNotFoundException("file with mapping not found");
        }
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
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
                try {
                    idNode = reader.nextInt();
                } catch (IllegalStateException | NumberFormatException e) {
                    throw new IllegalArgumentException("the value of idNode token must be an int");
                }
            } else if (nameField.equals("pathFile")) {
                try {
                    pathFile = reader.nextString();
                } catch (IllegalStateException e) {
                    throw new IllegalArgumentException("the value of pathFile token must be a String not null");
                }
            } else if (nameField.equals("tarceName")) {
                try {
                    tarceName = reader.nextString();
                } catch (IllegalStateException e) {
                    throw new IllegalArgumentException("the value of tarceName token, if present, must be a String not null");
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new MappingTrace(Objects.requireNonNull(idNode, "node id mustn't is null"), 
                                Objects.requireNonNull(pathFile, "file path mustn't is null"),
                                Optional.ofNullable(tarceName));

    }

}
