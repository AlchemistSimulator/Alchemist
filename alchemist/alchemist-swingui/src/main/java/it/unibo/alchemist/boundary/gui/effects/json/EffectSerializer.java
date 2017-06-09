package it.unibo.alchemist.boundary.gui.effects.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import it.unibo.alchemist.boundary.gui.effects.DrawColoredDot;
import it.unibo.alchemist.boundary.gui.effects.DrawDot;
import it.unibo.alchemist.boundary.gui.effects.DrawShapeFX;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;

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
public final class EffectSerializer {

    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize
     * {@link EffectFX effects} properly.
     */
    private static final RuntimeTypeAdapterFactory<EffectFX> RTA = RuntimeTypeAdapterFactory.of(EffectFX.class)
            .registerSubtype(DrawShapeFX.class, DrawShapeFX.class.toString()).registerSubtype(DrawDot.class, DrawDot.class.toString())
            .registerSubtype(DrawColoredDot.class, DrawColoredDot.class.toString());

    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize
     * {@link EffectGroup effect groups} properly.
     */
    private static final RuntimeTypeAdapterFactory<EffectGroup> RTA_GROUP = RuntimeTypeAdapterFactory.of(EffectGroup.class)
            .registerSubtype(EffectStack.class, EffectStack.class.toString());

    /**
     * Google gson object that concretely serializes and deserializes objects.
     */
    private static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory(RTA).registerTypeAdapterFactory(RTA_GROUP)
            .registerTypeAdapter(EffectGroup.class, new EffectGroupAdapter()).setPrettyPrinting().enableComplexMapKeySerialization()
            .create();

    /**
     * Default private, empty constructor, as it's an utility class.
     */
    private EffectSerializer() {
        // Empty constructor
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
        final EffectGroup effects = GSON.fromJson(reader, new TypeToken<EffectGroup>() {
        }.getType());
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
        GSON.toJson(effects, new TypeToken<List<EffectFX>>() {
        }.getType(), writer);
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
        final EffectGroup[] effectGroups = GSON.fromJson(reader, new TypeToken<EffectGroup[]>() {
        }.getType());
        reader.close();
        return Arrays.asList(effectGroups);
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
        GSON.toJson(effects.toArray(new EffectGroup[effects.size()]), new TypeToken<List<EffectGroup[]>>() {
        }.getType(), writer);
        writer.close();
    }
}
