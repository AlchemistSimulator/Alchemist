package it.unibo.alchemist.boundary.gui.effects.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.sun.javafx.binding.ExpressionHelper;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;
import javafx.beans.value.ObservableValue;

/**
 * Serialize Alchemist {@link EffectGroup effect groups} from/to file in human
 * readable format (JSON).
 * <p>
 * This class can be considered a clean boundary between Google Gson library and
 * the needs of this project, providing methods to serialize and deserialize
 * from JSON files instances of EffectGroup.
 * 
 * @see Gson
 */
@SuppressWarnings("restriction") // for class com.sun.javafx.binding.ExpressionHelper
public final class EffectSerializer {
    /** Reflection object for main Alchemist package. */
    private static final Reflections REFLECTIONS = new Reflections("it.unibo.alchemist");
    /** Set of available {@link EffectFX effect}s found by reflection. */
    private static final Set<Class<? extends EffectFX>> EFFECTS = REFLECTIONS.getSubTypesOf(EffectFX.class);

    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize
     * {@link EffectFX effects} properly.
     */
    private static final RuntimeTypeAdapterFactory<EffectFX> RTA = RuntimeTypeAdapterFactory.of(EffectFX.class);

    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize
     * {@link EffectGroup effect groups} properly.
     */
    private static final RuntimeTypeAdapterFactory<EffectGroup> RTA_GROUP = RuntimeTypeAdapterFactory.of(EffectGroup.class)
            .registerSubtype(EffectStack.class);

    static {
        // EffectFX subtypes are registered dynamically
        EFFECTS.forEach(RTA::registerSubtype);
    }

    /**
     * Google gson object that concretely serializes and deserializes objects.
     */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(RTA)
            .registerTypeAdapterFactory(RTA_GROUP)
            .registerTypeAdapter(EffectGroup.class, new EffectGroupAdapter())
            .registerTypeAdapter(new TypeToken<ExpressionHelper<Number>>() { }.getType(), initToNull())
            .registerTypeAdapter(new TypeToken<ObservableValue<Number>>() { }.getType(), initToNull())
            .registerTypeAdapter(new TypeToken<ExpressionHelper<String>>() { }.getType(), initToNull())
            .registerTypeAdapter(new TypeToken<ObservableValue<String>>() { }.getType(), initToNull())
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();

    /**
     * Default private, empty constructor, as it's an utility class.
     */
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
        // Try to deserialize a JSON file at first
        final Reader reader = new FileReader(effectFile);
        final EffectFX effect = GSON.fromJson(reader, new TypeToken<EffectFX>() { }.getType());
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
        GSON.toJson(effect, new TypeToken<EffectFX>() { }.getType(), writer);
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
        // Try to deserialize a JSON file at first
        final Reader reader = new FileReader(effectFile);
        final EffectGroup effects = GSON.fromJson(reader, new TypeToken<EffectGroup>() { }.getType());
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
        GSON.toJson(effects, new TypeToken<EffectGroup>() { }.getType(), writer);
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
        final List<EffectGroup> effectGroups = GSON.fromJson(reader, new TypeToken<EffectGroup[]>() { }.getType());
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
        GSON.toJson(effects, new TypeToken<List<EffectGroup>>() { }.getType(), writer);
        writer.close();
    }

    /**
     * @param <T> a generic type
     * @return an {@link InstanceCreator} that will serialize the object as null
     */
    private static <T> InstanceCreator<T> initToNull() {
        return (InstanceCreator<T>) a -> null;
    }
}
