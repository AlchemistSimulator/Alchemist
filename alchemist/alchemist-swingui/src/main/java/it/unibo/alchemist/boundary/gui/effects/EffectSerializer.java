package it.unibo.alchemist.boundary.gui.effects;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.danilopianini.io.FileUtilities;
import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.ImmutableCollectionWithCurrentElement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import it.unibo.alchemist.SupportedIncarnations;

/**
 * Serialize Alchemist effects from/to file in human readable format.
 *
 */
public final class EffectSerializer {

    /*
     * TODO register newly-added-effect subtypes to this factory to
     * (de)serialize them properly
     */
    private static final RuntimeTypeAdapterFactory<EffectFX> RTA = RuntimeTypeAdapterFactory.of(EffectFX.class)
            .registerSubtype(DrawShapeFX.class, DrawShapeFX.class.toString())
            .registerSubtype(DrawDot.class, DrawDot.class.toString())
            .registerSubtype(DrawColoredDot.class, DrawColoredDot.class.toString());
    private static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory(RTA)
            .registerTypeHierarchyAdapter(CollectionWithCurrentElement.class,
                    new TypeAdapter<ImmutableCollectionWithCurrentElement<?>>() {
                        @Override
                        public void write(final JsonWriter out, final ImmutableCollectionWithCurrentElement<?> value)
                                throws IOException {
                            out.value(value.getCurrent().toString());
                        }

                        @Override
                        public ImmutableCollectionWithCurrentElement<?> read(final JsonReader in) throws IOException {
                            return new ImmutableCollectionWithCurrentElement<String>(
                                    SupportedIncarnations.getAvailableIncarnations(), in.nextString());
                        }
                    })
            .setPrettyPrinting().create();

    private EffectSerializer() {
    }

    /**
     * Get a list of effects from the specified file. Try to deserialize a JSON
     * file at first. If this operation is not successful (for the sake of
     * backward compatibility) try to deserialize a binary file.
     * 
     * @param effectFile
     *            Source file
     * @return List of the effects collected from the file
     * @throws IOException
     *             Exception in handling the file
     * @throws ClassNotFoundException
     *             In case the serialized binary object is not an effect
     */
    @SuppressWarnings("unchecked")
    public static List<EffectFX> effectsFromFile(final File effectFile) throws IOException, ClassNotFoundException {
        // Try to deserialize a JSON file at first
        final Reader fr = new FileReader(effectFile);
        try {
            final List<EffectFX> effects = GSON.fromJson(fr, new TypeToken<List<EffectFX>>() {
            }.getType());
            fr.close();
            return effects;
        } catch (Exception e) {
            fr.close();
            final Object res = FileUtilities.fileToObject(effectFile);
            if (res instanceof EffectFX) {
                final List<EffectFX> effects = new ArrayList<>();
                effects.add((EffectFX) res);
                return effects;
            }
            // Backward compatibility: try to deserialize a binary file
            return (List<EffectFX>) FileUtilities.fileToObject(effectFile);
        }
    }

    /**
     * Write the given effect to the destination file.
     * 
     * @param effectFile
     *            Destination file
     * @param effect
     *            Effect
     * @throws IOException
     *             Exception in handling the file
     */
    public static void effectToFile(final File effectFile, final EffectFX effect) throws IOException {
        final List<EffectFX> effects = new ArrayList<>();
        effects.add(effect);
        effectsToFile(effectFile, effects);
    }

    /**
     * Write the given effects to the destination file.
     * 
     * @param effectFile
     *            Destination file
     * @param effects
     *            List of effects
     * @throws IOException
     *             Exception in handling the file
     */
    public static void effectsToFile(final File effectFile, final List<EffectFX> effects) throws IOException {
        final Writer fw = new FileWriter(effectFile);
        GSON.toJson(effects, new TypeToken<List<EffectFX>>() {
        }.getType(), fw);
        fw.close();
    }
}
