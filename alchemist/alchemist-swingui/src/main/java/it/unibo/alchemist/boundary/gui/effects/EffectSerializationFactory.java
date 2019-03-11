/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
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
public final class EffectSerializationFactory {

    /*
     * TODO register newly-added-effect subtypes to this factory to
     * (de)serialize them properly
     */
    private static final RuntimeTypeAdapterFactory<Effect> RTA = RuntimeTypeAdapterFactory.of(Effect.class)
            .registerSubtype(DrawShape.class, DrawShape.class.toString());
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
    @SuppressWarnings("unchecked")
    public static List<Effect> effectsFromFile(final File effectFile) throws IOException, ClassNotFoundException {
        // Try to deserialize a JSON file at first
        final Reader fr = new FileReader(effectFile);
        try {
            final List<Effect> effects = GSON.fromJson(fr, new TypeToken<List<Effect>>() {
            }.getType());
            fr.close();
            return effects;
        } catch (Exception e) {
            fr.close();
            final Object res = FileUtilities.fileToObject(effectFile);
            if (res instanceof Effect) {
                final List<Effect> effects = new ArrayList<Effect>();
                effects.add((Effect) res);
                return effects;
            }
            // Backward compatibility: try to deserialize a binary file
            return (List<Effect>) FileUtilities.fileToObject(effectFile);
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
        final List<Effect> effects = new ArrayList<Effect>();
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
    public static void effectsToFile(final File effectFile, final List<Effect> effects) throws IOException {
        final Writer fw = new FileWriter(effectFile);
        GSON.toJson(effects, new TypeToken<List<Effect>>() {
        }.getType(), fw);
        fw.close();
    }
}
