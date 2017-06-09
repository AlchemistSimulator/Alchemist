package it.unibo.alchemist.boundary.gui.effects.json;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;

/**
 * This class should be registered in a {@link GsonBuilder} to serialize and
 * deserialize a {@link EffectFX} compatible class.
 */
public class EffectAdapter implements JsonSerializer<EffectFX>, JsonDeserializer<EffectFX> {

    @Override
    public EffectFX deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonElement serialize(final EffectFX src, final Type typeOfSrc, final JsonSerializationContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}
