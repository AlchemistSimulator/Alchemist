package it.unibo.alchemist.boundary.gui.effects.json;

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
import it.unibo.alchemist.model.interfaces.Position2D;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class should be registered in a {@link com.google.gson.GsonBuilder} to
 * serialize and deserialize a {@link EffectGroup} compatible class.
 *
 * @param <P> The position type
 */
public class EffectGroupAdapter<P extends Position2D<? extends P>>  implements JsonSerializer<EffectGroup<P>>, JsonDeserializer<EffectGroup<P>> {
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
    private final Type effectsListType = new TypeToken<List<EffectFX<P>>>() {
    }.getType();

    /**
     * @inheritDocs
     */
    @Override
    public EffectGroup<P> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jObj = json.getAsJsonObject();

        final String name = jObj.get(NAME).getAsString();
        final EffectGroup<P> group = new EffectStack<>(name);
        final boolean visibility = jObj.get(VISIBILITY).getAsBoolean();
        group.setVisibility(visibility);
        final List<EffectFX<P>> effects = context.deserialize(jObj.get(EFFECTS), effectsListType);
        group.addAll(effects);

        return group;
    }

    /**
     * @inheritDocs
     */
    @Override
    public JsonElement serialize(final EffectGroup<P> src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jObj = new JsonObject();

        jObj.addProperty(NAME, src.getName());
        jObj.addProperty(VISIBILITY, src.isVisible());
        final List<EffectFX<P>> list = new ArrayList<>(src);
//        list.addAll(src);
        final JsonElement effects = context.serialize(list, effectsListType);
        jObj.add(EFFECTS, effects);

        return jObj;
    }

}
