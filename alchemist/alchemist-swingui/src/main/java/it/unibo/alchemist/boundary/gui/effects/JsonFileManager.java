package it.unibo.alchemist.boundary.gui.effects;

import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class models a clean boundary between Google Gson library and the needs
 * of this project, providing methods to serialize and deserialize from JSON
 * files instances of EffectGroup.
 */
public class JsonFileManager {
    private String lastPath;
    private final Gson gson;

    /**
     * Default constructor.
     */
    public JsonFileManager() {
        gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
        // TODO check
    }

    /**
     * 
     * @param path
     * @return
     */
    public List<EffectGroup> loadFromJSON(final String path) {
        // TODO
        return null;
    }

    /**
     * 
     * @param path
     * @param effect
     */
    public void saveToJSON(final String path, final Collection<EffectGroup> effect) {
        // TODO
    }
}
