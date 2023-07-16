/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.effects.serialization;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.unibo.alchemist.util.ClassPathScanner;
import it.unibo.alchemist.boundary.fxui.EffectFX;
import it.unibo.alchemist.boundary.fxui.EffectGroup;
import it.unibo.alchemist.model.Position2D;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.Property;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.TestOnly;
import org.kaikikm.threadresloader.ResourceLoader;

/**
 * Serialize Alchemist {@link EffectGroup effect groups} from/to file in human-readable format (JSON).
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

    private static final String RAWTYPES = "rawtypes";
    /**
     * Default extension of serialized groups of effects.
     */
    public static final String DEFAULT_EXTENSION = ".json";
    /**
     * Default charset of serialized groups of effects.
     */
    public static final Charset DEFAULT_CHARSET = Charsets.UTF_8;
    /**
     * {@code Type} of an {@code EffectGroup}.
     */
    private static final TypeToken<EffectGroup<?>> EFFECT_GROUP_TYPE = new TypeToken<>() {
    };
    /**
     * Set of available {@link EffectFX effect}s found by reflection.
     */
    @SuppressWarnings(RAWTYPES)
    private static final List<Class<? extends EffectFX>> EFFECTS =
            ClassPathScanner.subTypesOf(EffectFX.class, "it.unibo.alchemist");
    /**
     * Set of available {@link EffectGroup group}s found by reflection.
     */
    @SuppressWarnings(RAWTYPES)
    private static final List<Class<? extends EffectGroup>> GROUPS =
            ClassPathScanner.subTypesOf(EffectGroup.class, "it.unibo.alchemist");
    /**
     * Set of available {@link Property Properties} found by reflection.
     */
    @SuppressWarnings(RAWTYPES) // Needed to make the compiler accept the constant
    private static final List<Class<? extends Property>> PROPERTIES =
            ClassPathScanner.subTypesOf(Property.class, "it.unibo.alchemist");
    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize {@link EffectFX effects} properly.
     */
    @SuppressWarnings(RAWTYPES)
    private static final RuntimeTypeAdapterFactory<EffectFX> RTA_EFFECT =
            RuntimeTypeAdapterFactory.of(EffectFX.class);
    /**
     * {@link RuntimeTypeAdapterFactory} to serialize and deserialize {@link EffectGroup effect groups} properly.
     */
    @SuppressWarnings(RAWTYPES)
    private static final RuntimeTypeAdapterFactory<EffectGroup> RTA_GROUP =
            RuntimeTypeAdapterFactory.of(EffectGroup.class);
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
        registerTypeAdaptersFor(builder, PROPERTIES);
        registerTypeAdaptersFor(builder, GROUPS);
        GSON = builder
                .registerTypeAdapterFactory(RTA_EFFECT)
                .registerTypeAdapterFactory(RTA_GROUP)
                .registerTypeAdapter(EFFECT_GROUP_TYPE.getType(), new EffectGroupAdapter<>())
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
    }

    /**
     * Default private, empty constructor, as this is a utility class.
     */
    private EffectSerializer() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * The method registers a {@link com.google.gson.TypeAdapter} for given {@link Class classes}.
     * <p>
     * The class must have a method called {@value #TARGET_METHOD_NAME}.
     *
     * @param builder the {@code GsonBuilder} to {@link GsonBuilder#registerTypeAdapter(java.lang.reflect.Type, Object)
     *      register TypeAdapter} to
     * @param classes the class to {@link GsonBuilder#registerTypeAdapter(java.lang.reflect.Type, Object)
     *      register TypeAdapter} for
     * @param <T>     the type of class
     */
    private static <T> void registerTypeAdaptersFor(
            final GsonBuilder builder,
            final Collection<Class<? extends T>> classes
    ) {
        classes.stream()
                .filter(c -> Arrays.stream(c.getMethods())
                        .filter(m -> Modifier.isStatic(m.getModifiers()))
                        .anyMatch(m -> TARGET_METHOD_NAME.equals(m.getName())))
                .forEach(c -> {
                    try {
                        builder.registerTypeAdapter(c, c.getMethod(TARGET_METHOD_NAME).invoke(null));
                    } catch (final NoSuchMethodException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException | SecurityException e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    /**
     * Loads a generic file from a generic reader and closes that reader.
     *
     * @param reader a specified reader to use
     * @param type   a specified type token to get right type from
     * @param <T>    the generic type of object to load
     * @return the serialized JSON object
     * @throws com.google.gson.JsonIOException     If there was a problem reading from the Reader
     * @throws com.google.gson.JsonSyntaxException If JSON is not a valid representation for an object of type
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
     * @throws com.google.gson.JsonIOException If there was a problem writing to the writer
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
     * @param <P> the position type
     * @param effectFile Source file
     * @return Effect loaded from the file
     * @throws java.io.FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws com.google.gson.JsonIOException       If there was a problem reading from the Reader
     * @throws com.google.gson.JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static <P extends Position2D<? extends P>> EffectFX<P> effectFromFile(
            final File effectFile
    ) throws IOException {
        return load(new FileReader(effectFile, DEFAULT_CHARSET), new TypeToken<>() { });
    }

    /**
     * Get an {@link EffectFX Effect} from the specified resource file. It tries to
     * deserialize a JSON file.
     *
     * @param <P> the position type
     * @param resource resource file
     * @return Effect loaded from the resource file
     * @throws java.io.FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws com.google.gson.JsonIOException       If there was a problem reading from the Reader
     * @throws com.google.gson.JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static <P extends Position2D<? extends P>> EffectFX<P> effectFromResources(
            final String resource
    ) throws IOException {
        return load(
                new InputStreamReader(ResourceLoader.getResourceAsStream(resource), DEFAULT_CHARSET),
                new TypeToken<>() { }
            );
    }

    /**
     * Write the given {@link EffectFX} to the destination file.
     *
     * @param <P> the position type
     * @param effectFile Destination file
     * @param effect     Effect
     * @throws com.google.gson.JsonIOException If there was a problem writing to the writer
     * @throws IOException     If the file exists but is a directory rather than a regular
     *                         file, does not exist but cannot be created, cannot be opened
     *                         for any other reason, or another I/O error occurs
     */
    public static <P extends Position2D<? extends P>> void effectToFile(
            final File effectFile,
            final EffectFX<P> effect
    ) throws IOException {
        save(new FileWriter(effectFile, DEFAULT_CHARSET), effect, new TypeToken<>() { });
    }

    /**
     * Get an {@link EffectGroup} from the specified file. It tries to
     * deserialize a JSON file.
     *
     * @param <P> the position type
     * @param effectFile Source file
     * @return Group of effects collected from the file
     * @throws java.io.FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws com.google.gson.JsonIOException       If there was a problem reading from the Reader
     * @throws com.google.gson.JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static <P extends Position2D<? extends P>> EffectGroup<P> effectsFromFile(
            final File effectFile
    ) throws IOException {
        return load(new FileReader(effectFile, DEFAULT_CHARSET), new TypeToken<>() { });
    }

    /**
     * Get an {@link EffectGroup} from the specified resource file. It tries to
     * deserialize a JSON file.
     *
     * @param <P> the position type
     * @param resource resource file
     * @return Group of effects collected from the file
     * @throws java.io.FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws com.google.gson.JsonIOException       If there was a problem reading from the Reader
     * @throws com.google.gson.JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static <P extends Position2D<? extends P>> EffectGroup<P> effectsFromResources(
            final String resource
    ) throws IOException {
        return load(
                new InputStreamReader(ResourceLoader.getResourceAsStream(resource), DEFAULT_CHARSET),
                new TypeToken<>() { }
            );
    }

    /**
     * Write the given {@link EffectGroup} to the destination file.
     *
     * @param <P> the position type
     * @param effectFile Destination file
     * @param effects    Group of effects
     * @throws com.google.gson.JsonIOException If there was a problem writing to the writer
     * @throws IOException     If the file exists but is a directory rather than a regular
     *                         file, does not exist but cannot be created, cannot be opened
     *                         for any other reason, or another I/O error occurs
     */
    public static <P extends Position2D<? extends P>> void effectsToFile(
            final File effectFile,
            final EffectGroup<P> effects
    ) throws IOException {
        save(new FileWriter(effectFile, DEFAULT_CHARSET), effects, new TypeToken<>() { });
    }

    /**
     * Get a list of {@link EffectGroup} from the specified file. It tries to
     * deserialize a JSON file.
     *
     * @param <P> the position type
     * @param effectFile Source file
     * @return List of the effect groups collected from the file
     * @throws java.io.FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws com.google.gson.JsonIOException       If there was a problem reading from the Reader
     * @throws com.google.gson.JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static <P extends Position2D<? extends P>> List<EffectGroup<P>> effectGroupsFromFile(
            final File effectFile
    ) throws IOException {
        return load(new FileReader(effectFile, DEFAULT_CHARSET), new TypeToken<>() { });
    }

    /**
     * Get a list of {@link EffectGroup} from the specified resource file. It tries to
     * deserialize a JSON file.
     *
     * @param <P> the position type
     * @param resource resource file
     * @return List of the effect groups collected from the file
     * @throws java.io.FileNotFoundException If the file does not exist, is a directory rather than a
     *                               regular file, or for some other reason cannot be opened for
     *                               reading
     * @throws com.google.gson.JsonIOException       If there was a problem reading from the Reader
     * @throws com.google.gson.JsonSyntaxException   If JSON is not a valid representation for an object of type
     * @throws IOException           If some other I/O error occurs
     */
    public static <P extends Position2D<? extends P>> List<EffectGroup<P>> effectGroupsFromResources(
            final String resource
    ) throws IOException {
        return load(
                new InputStreamReader(ResourceLoader.getResourceAsStream(resource), DEFAULT_CHARSET),
                new TypeToken<>() { }
            );
    }

    /**
     * Write the given list of {@link EffectGroup}s to the destination file.
     *
     * @param <P> the position type
     * @param effectFile Destination file
     * @param effects    List of group of effects
     * @throws com.google.gson.JsonIOException If there was a problem writing to the writer
     * @throws IOException     If the file exists but is a directory rather than a regular
     *                         file, does not exist but cannot be created, cannot be opened
     *                         for any other reason, or another I/O error occurs
     */
    public static <P extends Position2D<? extends P>> void effectGroupsToFile(
            final File effectFile,
            final List<EffectGroup<P>> effects
    ) throws IOException {
        save(new FileWriter(effectFile, DEFAULT_CHARSET), effects, new TypeToken<>() { });
    }

    /**
     * Returns the internal static instance of {@link Gson} object used for
     * serialization. It includes all needed {@link com.google.gson.TypeAdapter}s for
     * {@link EffectFX Effects} and {@link Property Properties} serialization.
     * <p>
     * Useful for serialize related objects not directly managed by this class.
     *
     * @return the {@code Gson} object for serialization
     */
    @Contract(pure = true)
    @TestOnly
    public static Gson getGSON() {
        return GSON;
    }
}
