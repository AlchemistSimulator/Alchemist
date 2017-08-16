package it.unibo.alchemist.boundary.gui.effects.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.view.properties.PropertyTypeAdapter;
import javafx.beans.property.Property;
import javassist.Modifier;

/**
 * Serialize Alchemist {@link EffectGroup effect groups} from/to file in human
 * readable format (JSON).
 * <p>
 * This class can be considered a clean boundary between Google Gson library and
 * the needs of this project, providing methods to serialize and deserialize
 * from JSON files instances of EffectGroup.
 * <p>
 * The {@link Gson GSON} object used for serialization is statically updated at
 * runtime with all available {@code TypeAdapters} and
 * {@code RuntimeTypeAdapter}.
 * 
 * @see Gson
 */
public final class EffectSerializer {
    /** Default extension of serialized groups of effects. */
    public static final String DEFAULT_EXTENSION = ".json";

    /** {@code Type} of an {@code EffectFX}. */
    private static final Type EFFECT_TYPE = new TypeToken<EffectFX>() { }.getType();
    /** {@code Type} of an {@code EffectGroup}. */
    private static final Type EFFECT_GROUP_TYPE = new TypeToken<EffectGroup>() { }.getType();
    /** @code Type} of a {@code List} of {@code EffectGroup}. */
    private static final Type EFFECT_GROUP_LIST_TYPE = new TypeToken<List<EffectGroup>>() { }.getType();

    /** Reflection object for main Alchemist package. */
    private static final Reflections REFLECTIONS = new Reflections("it.unibo.alchemist");
    /** Set of available {@link EffectFX effect}s found by reflection. */
    private static final Set<Class<? extends EffectFX>> EFFECTS = REFLECTIONS.getSubTypesOf(EffectFX.class);
    /** Set of available {@link EffectGroup group}s found by reflection. */
    private static final Set<Class<? extends EffectGroup>> GROUPS = REFLECTIONS.getSubTypesOf(EffectGroup.class);
    /** Set of available {@link Property Properties} found by reflection. */
    @SuppressWarnings("rawtypes") // Needed to make the compiler accept the constant
    private static final Set<Class<? extends Property>> PROPERTIES = REFLECTIONS.getSubTypesOf(Property.class);
    /** Map of all available {@link PropertyTypeAdapter}. */
    private static final Map<Class<?>, PropertyTypeAdapter<?>> PROPERTY_TYPE_ADAPTER = new HashMap<>();
    /** {@link RuntimeTypeAdapterFactory} to serialize and deserialize {@link EffectFX effects} properly. */
    private static final RuntimeTypeAdapterFactory<EffectFX> RTA_EFFECT = RuntimeTypeAdapterFactory.of(EffectFX.class);
    /** {@link RuntimeTypeAdapterFactory} to serialize and deserialize {@link EffectGroup effect groups} properly. */
    private static final RuntimeTypeAdapterFactory<EffectGroup> RTA_GROUP = RuntimeTypeAdapterFactory.of(EffectGroup.class);
    /** Target method that will return the {@code TypeAdapter} for the property. */
    private static final String TARGET_METHOD_NAME = "getTypeAdapter";

    /** Google GSON object that concretely serializes and deserializes objects. */
    private static final Gson GSON;

    /* Dynamically load TypeAdapters in GSON object */
    static {
        EFFECTS.forEach(RTA_EFFECT::registerSubtype);
        GROUPS.forEach(RTA_GROUP::registerSubtype);

        final GsonBuilder builder = new GsonBuilder();

        PROPERTIES.stream()
            .filter(c -> Arrays.stream(c.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .anyMatch(m -> m.getName().equals(TARGET_METHOD_NAME)))
            .forEach(c -> {
                try {
                    builder.registerTypeAdapter(c, c.getMethod(TARGET_METHOD_NAME).invoke(null));
                } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | SecurityException e) {
                    throw new IllegalStateException(e);
                }
            });

        GROUPS.stream()
        .filter(c -> Arrays.stream(c.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .anyMatch(m -> m.getName().equals(TARGET_METHOD_NAME)))
            .forEach(c -> {
                try {
                    builder.registerTypeAdapter(c, c.getMethod(TARGET_METHOD_NAME).invoke(null));
                } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | SecurityException e) {
                    throw new IllegalStateException(e);
                }
            });

        PROPERTY_TYPE_ADAPTER.forEach(builder::registerTypeAdapter);

        GSON = builder
                .registerTypeAdapterFactory(RTA_EFFECT)
                .registerTypeAdapterFactory(RTA_GROUP)
                .registerTypeAdapter(EFFECT_GROUP_TYPE, new EffectGroupAdapter())
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
    }

    /** Default private, empty constructor, as this is an utility class. */
    private EffectSerializer() {
        // Empty constructor
    }

    /**
     * Get an {@link EffectFX Effect} from the specified file. It tries to
     * deserialize a JSON file.
     * 
     * @param effectFile
     *            Source file
     * @return Effect loaded from the file
     * @throws FileNotFoundException
     *             If the file does not exist, is a directory rather than a
     *             regular file, or for some other reason cannot be opened for
     *             reading
     * @throws JsonIOException
     *             If there was a problem reading from the Reader
     * @throws JsonSyntaxException
     *             If JSON is not a valid representation for an object of type
     * @throws IOException
     *             If some other I/O error occurs
     */
    public static EffectFX effectFromFile(final File effectFile) throws IOException {
        final Reader reader = new FileReader(effectFile);
        final EffectFX effect = GSON.fromJson(reader, EFFECT_TYPE);
        reader.close();
        return effect;
    }

    /**
     * Write the given {@link EffectFX} to the destination file.
     * 
     * @param effectFile
     *            Destination file
     * @param effect
     *            Effect
     * @throws JsonIOException
     *             If there was a problem writing to the writer
     * @throws IOException
     *             If the file exists but is a directory rather than a regular
     *             file, does not exist but cannot be created, cannot be opened
     *             for any other reason, or another I/O error occurs
     */
    public static void effectToFile(final File effectFile, final EffectFX effect) throws IOException {
        final Writer writer = new FileWriter(effectFile);
        GSON.toJson(effect, EFFECT_TYPE, writer);
        writer.close();
    }

    /**
     * Get an {@link EffectGroup} from the specified file. It tries to
     * deserialize a JSON file.
     * 
     * @param effectFile
     *            Source file
     * @return Group of effects collected from the file
     * @throws FileNotFoundException
     *             If the file does not exist, is a directory rather than a
     *             regular file, or for some other reason cannot be opened for
     *             reading
     * @throws JsonIOException
     *             If there was a problem reading from the Reader
     * @throws JsonSyntaxException
     *             If JSON is not a valid representation for an object of type
     * @throws IOException
     *             If some other I/O error occurs
     */
    public static EffectGroup effectsFromFile(final File effectFile) throws IOException {
        final Reader reader = new FileReader(effectFile);
        final EffectGroup effects = GSON.fromJson(reader, EFFECT_GROUP_TYPE);
        reader.close();
        return effects;
    }

    /**
     * Write the given {@link EffectGroup} to the destination file.
     * 
     * @param effectFile
     *            Destination file
     * @param effects
     *            Group of effects
     * @throws JsonIOException
     *             If there was a problem writing to the writer
     * @throws IOException
     *             If the file exists but is a directory rather than a regular
     *             file, does not exist but cannot be created, cannot be opened
     *             for any other reason, or another I/O error occurs
     */
    public static void effectsToFile(final File effectFile, final EffectGroup effects) throws IOException {
        final Writer writer = new FileWriter(effectFile);
        GSON.toJson(effects, EFFECT_GROUP_TYPE, writer);
        writer.close();
    }

    /**
     * Get a list of {@link EffectGroup} from the specified file. It tries to
     * deserialize a JSON file.
     * 
     * @param effectFile
     *            Source file
     * @return List of the effect groups collected from the file
     * @throws FileNotFoundException
     *             If the file does not exist, is a directory rather than a
     *             regular file, or for some other reason cannot be opened for
     *             reading
     * @throws JsonIOException
     *             If there was a problem reading from the Reader
     * @throws JsonSyntaxException
     *             If JSON is not a valid representation for an object of type
     * @throws IOException
     *             If some other I/O error occurs
     */
    public static List<EffectGroup> effectGroupsFromFile(final File effectFile) throws IOException {
        final Reader reader = new FileReader(effectFile);
        final List<EffectGroup> effectGroups = GSON.fromJson(reader, EFFECT_GROUP_LIST_TYPE);
        reader.close();
        return effectGroups;
    }

    /**
     * Write the given list of {@link EffectGroup}s to the destination file.
     * 
     * @param effectFile
     *            Destination file
     * @param effects
     *            List of group of effects
     * @throws JsonIOException
     *             If there was a problem writing to the writer
     * @throws IOException
     *             If the file exists but is a directory rather than a regular
     *             file, does not exist but cannot be created, cannot be opened
     *             for any other reason, or another I/O error occurs
     */
    public static void effectGroupsToFile(final File effectFile, final List<EffectGroup> effects) throws IOException {
        final Writer writer = new FileWriter(effectFile);
        GSON.toJson(effects, EFFECT_GROUP_LIST_TYPE, writer);
        writer.close();
    }

    /**
     * Returns the internal static instance of {@link Gson} object used for
     * serialization. It includes all needed {@link TypeAdapter}s for
     * {@link EffectFX Effects} and {@link Property Properties} serialization.
     * <p>
     * Useful for serialize related objects not directly managed by this class.
     * 
     * @return the {@code Gson} object for serialization
     */
    public static Gson getGSON() {
        return GSON;
    }
}
