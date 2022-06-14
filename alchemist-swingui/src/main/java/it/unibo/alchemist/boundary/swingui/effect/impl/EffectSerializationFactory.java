/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.effect.impl;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.boundary.swingui.effect.api.LayerToFunctionMapper;
import it.unibo.alchemist.model.api.SupportedIncarnations;
import it.unibo.alchemist.util.ClassPathScanner;
import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.ImmutableCollectionWithCurrentElement;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.List;

/**
 * Serialize Alchemist effects from/to file in human readable format.
 *
 */
@Deprecated
public final class EffectSerializationFactory {

    private static final Gson GSON;

    static {
        final var builder = new GsonBuilder()
            .registerTypeHierarchyAdapter(
                CollectionWithCurrentElement.class,
                new TypeAdapter<ImmutableCollectionWithCurrentElement<?>>() {
                    @Override
                    public void write(final JsonWriter out, final ImmutableCollectionWithCurrentElement<?> value)
                            throws IOException {
                        out.value(value.getCurrent().toString());
                    }
                    @Override
                    public ImmutableCollectionWithCurrentElement<?> read(final JsonReader in) throws IOException {
                        return new ImmutableCollectionWithCurrentElement<>(
                            SupportedIncarnations.getAvailableIncarnations(),
                            in.nextString()
                        );
                    }
                }
            )
            .registerTypeHierarchyAdapter(
                Color.class,
                new TypeAdapter<Color>() {
                    @Override
                    public void write(final JsonWriter out, final Color value) throws IOException {
                        out.beginObject();
                        out.name("value");
                        out.value(value.getRGB());
                        out.endObject();
                    }
                    @Override
                    public Color read(final JsonReader in) throws IOException {
                        in.beginObject();
                        in.nextName();
                        final int value = in.nextInt();
                        while (in.peek() != JsonToken.END_OBJECT) {
                            in.skipValue();
                        }
                        in.endObject();
                        return new Color(value);
                    }
                }
            )
            .setPrettyPrinting();
        List.of(Effect.class, LayerToFunctionMapper.class).forEach(clazz -> {
            @SuppressWarnings("unchecked")
            final var runtimeAdapterFactory = (RuntimeTypeAdapterFactory<Serializable>) RuntimeTypeAdapterFactory.of(clazz);
            @SuppressWarnings("unchecked")
            final var legacyRuntimeAdapterFactory = (RuntimeTypeAdapterFactory<Serializable>) RuntimeTypeAdapterFactory.of(clazz);
            ClassPathScanner
                .subTypesOf(clazz, clazz.getPackageName(), "it.unibo.alchemist")
                .forEach(subtype -> {
                    runtimeAdapterFactory.registerSubtype(subtype, subtype.toString());
                    // Legacy effects support
                    legacyRuntimeAdapterFactory.registerSubtype(
                        subtype,
                        "class it.unibo.alchemist.boundary.gui.effects." + subtype.getSimpleName()
                    );
                });
            builder.registerTypeAdapterFactory(runtimeAdapterFactory);
            builder.registerTypeAdapterFactory(legacyRuntimeAdapterFactory);
        });
        GSON = builder.create();
    }

    private EffectSerializationFactory() {
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
    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "RuntimeException is willingly caught")
    public static List<Effect> effectsFromFile(final File effectFile) throws IOException, ClassNotFoundException {
        try (Reader fr = new InputStreamReader(new FileInputStream(effectFile), Charsets.UTF_8)) {
            return GSON.fromJson(fr, new TypeToken<List<Effect>>() { }.getType());
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
    public static void effectToFile(final File effectFile, final Effect effect) throws IOException {
        effectsToFile(effectFile, List.of(effect));
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
    public static void effectsToFile(final File effectFile, final List<Effect> effects) throws IOException {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(effectFile), Charsets.UTF_8)) {
            GSON.toJson(effects, new TypeToken<List<Effect>>() { }.getType(), fw);
        }
    }
}
