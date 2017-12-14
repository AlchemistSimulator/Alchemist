package it.unibo.alchemist.boundary.gui.effects.json;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class should be registered in a {@link GsonBuilder} to
 * serialize and deserialize a {@link EffectGroup} compatible class.
 */
public class EffectGroupAdapter implements JsonSerializer<EffectGroup>, JsonDeserializer<EffectGroup> {
    /**
     * Name given to {@code name} field in JSON file.
     */
    private static final String NAME = "name";
    /**
     * Name given to {@code visibility} field in JSON file.
     */
    private static final String VISIBILITY = "visibility";
    /**
     * Name given to {@code effects} list field in JSON file.
     */
    private static final String EFFECTS = "effects";
    /**
     * Type of {@link List}<{@link EffectFX}>.
     */
    private static final Type EFFECTS_LIST_TYPE = new TypeToken<List<EffectFX>>() {
    }.getType();

    @Override
    public EffectGroup deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jObj = json.getAsJsonObject();

        final String name = jObj.get(NAME).getAsString();
        final EffectGroup group = new EffectStack(name);
        final boolean visibility = jObj.get(VISIBILITY).getAsBoolean();
        group.setVisibility(visibility);
        final List<EffectFX> effects = context.deserialize(jObj.get(EFFECTS), EFFECTS_LIST_TYPE);
        group.addAll(effects);

        return group;
    }

    @Override
    public JsonElement serialize(final EffectGroup src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jObj = new JsonObject();

        jObj.addProperty(NAME, src.getName());
        jObj.addProperty(VISIBILITY, src.isVisible());
        final List<EffectFX> list = new ArrayList<>();
        list.addAll(src);
        final JsonElement effects = context.serialize(list, EFFECTS_LIST_TYPE);
        jObj.add(EFFECTS, effects);

        return jObj;
    }

}
