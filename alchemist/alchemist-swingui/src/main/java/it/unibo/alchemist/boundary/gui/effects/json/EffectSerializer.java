package it.unibo.alchemist.boundary.gui.effects.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javafx.beans.property.Property;
import javafx.scene.paint.Color;
import javassist.Modifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.TestOnly;
import org.reflections.Reflections;

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
    /**
     * Default extension of serialized groups of effects.
     */
    public static final String DEFAULT_EXTENSION = ".json";
    /**
     * Default charset of serialized groups of effects.
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * {@code Type} of an {@code EffectFX}.
     */
    private static final TypeToken<EffectFX> EFFECT_TYPE = new TypeToken<EffectFX>() { };
    /**
     * {@code Type} of an {@code EffectGroup}.
     */
    private static final TypeToken<EffectGroup> EFFECT_GROUP_TYPE = new TypeToken<EffectGroup>() { };
    /**
     * @code Type} of a {@code List} of {@code EffectGroup}.
     */
    private static final TypeToken<List<EffectGroup>> EFFECT_GROUP_LIST_TYPE = new TypeToken<List<EffectGroup>>() { };

    /**
     * Reflection object for main Alchemist package.
     */
    private static final Reflections REFLECTIONS = new Reflections("it.unibo.alchemist");
    /**
     * Set of available {@link EffectFX effect}s found by reflection.
     */
    private static final Set<Class<? extends EffectFX>> EFFECTS = REFLECTIONS.getSubTypesOf(EffectFX.class);
    /**
     * Set of available {@link EffectGroup group}s found by reflection.
     */
    private static final Set<Class<? extends EffectGroup>> GROUPS = REFLECTIONS.getSubTypesOf(EffectGroup.class);
    /**
     * Set of available {@link Property Properties} found by reflection.
     */
    @SuppressWarnings("rawtypes") // Needed to make the compiler accept the constant
    private static final Set<Class<? extends Property>> PROPERTIES = REFLECTIONS.getSubTypesOf(Property.class);
    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize {@link EffectFX effects} properly.
     */
    private static final RuntimeTypeAdapterFactory<EffectFX> RTA_EFFECT = RuntimeTypeAdapterFactory.of(EffectFX.class);
    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize {@link EffectGroup effect groups} properly.
     */
    private static final RuntimeTypeAdapterFactory<EffectGroup> RTA_GROUP = RuntimeTypeAdapterFactory.of(EffectGroup.class);
    /**
     * Target method that will return the {@code TypeAdapter} for the property.
     */
    private static final String TARGET_METHOD_NAME = "getTypeAdapter";

    /**
     * Google GSON object that concretely serializes and deserializes objects.
     */
    private static final Gson GSON;

    /* Dynamically load TypeAdapters in GSON object */
    static {
        EFFECTS.forEach(RTA_EFFECT::registerSubtype);
        GROUPS.forEach(RTA_GROUP::registerSubtype);
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(new TypeToken<Color>() {
        }.getType(), new ColorSerializationAdapter());
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
        GSON = builder
                .registerTypeAdapterFactory(RTA_EFFECT)
                .registerTypeAdapterFactory(RTA_GROUP)
                .registerTypeAdapter(EFFECT_GROUP_TYPE.getType(), new EffectGroupAdapter())
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
    }

    /**
     * Default private, empty constructor, as this is an utility class.
     */
    private EffectSerializer() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * Loads a generic file from a generic reader and closes that reader.
     *
     * @param reader a specified reader to use
     * @param type   a specified type token to get right type from
     * @param <T>    the generic type of object to load
     * @return the serialized JSON object
     * @throws JsonIOException     If there was a problem reading from the Reader
     * @throws JsonSyntaxException If JSON is not a valid representation for an object of type
     * @throws IOException         If some other I/O error occurs
     */
    private static <T> T load(final Reader reader, final TypeToken<T> type) throws IOException {
        final T deserialized = GSON.fromJson(reader, type.getType());
        reader.close();
        return deserialized;
    }

    /**
     * Saves a generic file with a generic writer and closes that writer.
     *
     * @param writer a specified writer to use
     * @param object the object to serialize
     * @param type   a specified type token to get right type from
     * @param <T>    the generic type of object to save
     * @throws JsonIOException If there was a problem writing to the writer
     * @throws IOException     If some other I/O error occurs
     */
    private static <T> void save(final Writer writer, final T object, final TypeToken<T> type) throws IOException {
        GSON.toJson(object, type.getType(), writer);
        writer.close();
    }

    /**
     * Get an {@link EffectFX Effect} from the specified file. It tries to
     * deserialize a JSON file.
     *
     * @param effectFile Source file
     * @return Effect loaded from the file
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws JsonIOException       If there was a problem reading from the Reader
     * @throws JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static EffectFX effectFromFile(final File effectFile) throws IOException {
        return load(new FileReader(effectFile), EFFECT_TYPE);
    }

    /**
     * Get an {@link EffectFX Effect} from the specified resource file. It tries to
     * deserialize a JSON file.
     *
     * @param resource resource file
     * @return Effect loaded from the resource file
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws JsonIOException       If there was a problem reading from the Reader
     * @throws JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static EffectFX effectFromResources(final String resource) throws IOException {
        return load(new InputStreamReader(ResourceLoader.load(resource), DEFAULT_CHARSET), EFFECT_TYPE);
    }

    /**
     * Write the given {@link EffectFX} to the destination file.
     *
     * @param effectFile Destination file
     * @param effect     Effect
     * @throws JsonIOException If there was a problem writing to the writer
     * @throws IOException     If the file exists but is a directory rather than a regular
     *                         file, does not exist but cannot be created, cannot be opened
     *                         for any other reason, or another I/O error occurs
     */
    public static void effectToFile(final File effectFile, final EffectFX effect) throws IOException {
        save(new FileWriter(effectFile), effect, EFFECT_TYPE);
    }

    /**
     * Get an {@link EffectGroup} from the specified file. It tries to
     * deserialize a JSON file.
     *
     * @param effectFile Source file
     * @return Group of effects collected from the file
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws JsonIOException       If there was a problem reading from the Reader
     * @throws JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static EffectGroup effectsFromFile(final File effectFile) throws IOException {
        return load(new FileReader(effectFile), EFFECT_GROUP_TYPE);
    }

    /**
     * Get an {@link EffectGroup} from the specified resource file. It tries to
     * deserialize a JSON file.
     *
     * @param resource resource file
     * @return Group of effects collected from the file
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws JsonIOException       If there was a problem reading from the Reader
     * @throws JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static EffectGroup effectsFromResources(final String resource) throws IOException {
        return load(new InputStreamReader(ResourceLoader.load(resource), DEFAULT_CHARSET), EFFECT_GROUP_TYPE);
    }

    /**
     * Write the given {@link EffectGroup} to the destination file.
     *
     * @param effectFile Destination file
     * @param effects    Group of effects
     * @throws JsonIOException If there was a problem writing to the writer
     * @throws IOException     If the file exists but is a directory rather than a regular
     *                         file, does not exist but cannot be created, cannot be opened
     *                         for any other reason, or another I/O error occurs
     */
    public static void effectsToFile(final File effectFile, final EffectGroup effects) throws IOException {
        save(new FileWriter(effectFile), effects, EFFECT_GROUP_TYPE);
    }

    /**
     * Get a list of {@link EffectGroup} from the specified file. It tries to
     * deserialize a JSON file.
     *
     * @param effectFile Source file
     * @return List of the effect groups collected from the file
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws JsonIOException       If there was a problem reading from the Reader
     * @throws JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static List<EffectGroup> effectGroupsFromFile(final File effectFile) throws IOException {
        return load(new FileReader(effectFile), EFFECT_GROUP_LIST_TYPE);
    }

    /**
     * Get a list of {@link EffectGroup} from the specified resource file. It tries to
     * deserialize a JSON file.
     *
     * @param resource resource file
     * @return List of the effect groups collected from the file
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws JsonIOException       If there was a problem reading from the Reader
     * @throws JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static List<EffectGroup> effectGroupsFromResources(final String resource) throws IOException {
        return load(new InputStreamReader(ResourceLoader.load(resource), DEFAULT_CHARSET), EFFECT_GROUP_LIST_TYPE);
    }

    /**
     * Write the given list of {@link EffectGroup}s to the destination file.
     *
     * @param effectFile Destination file
     * @param effects    List of group of effects
     * @throws JsonIOException If there was a problem writing to the writer
     * @throws IOException     If the file exists but is a directory rather than a regular
     *                         file, does not exist but cannot be created, cannot be opened
     *                         for any other reason, or another I/O error occurs
     */
    public static void effectGroupsToFile(final File effectFile, final List<EffectGroup> effects) throws IOException {
        save(new FileWriter(effectFile), effects, EFFECT_GROUP_LIST_TYPE);
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
    @Contract(pure = true)
    @TestOnly
    protected static Gson getGSON() {
        return GSON;
    }
}
