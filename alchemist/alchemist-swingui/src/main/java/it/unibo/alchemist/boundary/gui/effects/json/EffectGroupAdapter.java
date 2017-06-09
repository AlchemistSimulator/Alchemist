package it.unibo.alchemist.boundary.gui.effects.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;

/**
 * This class should be registered in a {@link com.google.gson.GsonBuilder} to
 * serialize and deserialize a {@link EffectGroup} compatible class.
 */
public class EffectGroupAdapter implements JsonSerializer<EffectGroup>, JsonDeserializer<EffectGroup> {
    private static final String NAME = "name";
    private static final String VISIBILITY = "visibility";
    private static final String TRANSPARENCY = "transparency";
    private static final String EFFECTS = "effects";
    private static final Type EFFECTS_MAP_TYPE = new TypeToken<Map<EffectFX, Boolean>>() {
        private static final long serialVersionUID = 4134289956171773132L;
    }.getType();

    @Override
    public EffectGroup deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jObj = (JsonObject) json;

        final EffectGroup group = new EffectStack(jObj.get(NAME).getAsString());
        group.setVisibility(jObj.get(VISIBILITY).getAsBoolean());
        group.setTransparency(jObj.get(TRANSPARENCY).getAsInt());

        final Map<EffectFX, Boolean> effects = context.deserialize(jObj.get(EFFECTS), EFFECTS_MAP_TYPE);

        effects.entrySet().forEach(e -> {
            group.add(e.getKey());
            group.setVisibilityOf(e.getKey(), e.getValue());
        });

        return group;
    }

    @Override
    public JsonElement serialize(final EffectGroup src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jObj = new JsonObject();

        jObj.addProperty(NAME, src.getName());
        jObj.addProperty(VISIBILITY, src.isVisible());
        jObj.addProperty(TRANSPARENCY, src.getTransparency());

        final Map<EffectFX, Boolean> effectsMap = new HashMap<>();
        src.forEach(e -> effectsMap.put(e, src.getVisibilityOf(e)));
        final JsonElement effects = context.serialize(effectsMap, EFFECTS_MAP_TYPE);

        jObj.add(EFFECTS, effects);

        return jObj;
    }

}
