/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.effects.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.scene.paint.Color;

/**
 * This class should be registered in a {@link com.google.gson.GsonBuilder} to
 * serialize and deserialize JavaFX {@link Color} objects.
 */
public class ColorSerializationAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
    private static final String RED = "Red";
    private static final String GREEN = "Green";
    private static final String BLUE = "Blue";
    private static final String ALPHA = "Alpha";

    /**
     * Generalized way to serialize JavaFX {@link Color} on a stream.
     *
     * @param stream the writeObject() outputStream
     * @param color  the color to serialize
     * @throws IOException if I/O errors occur while writing to the underlying stream
     */
    public static void writeColor(final ObjectOutputStream stream, final Color color) throws IOException {
        stream.writeDouble(color.getRed());
        stream.writeDouble(color.getGreen());
        stream.writeDouble(color.getBlue());
        stream.writeDouble(color.getOpacity());
    }

    /**
     * Generalized way to deserialize a JavaFX {@link Color} from a stream.
     *
     * @param stream the readObject() inputStream
     * @return the color to deserialize
     * @throws java.io.EOFException   if the end of file is reached
     * @throws IOException            if other I/O error has occurred
     */
    @Nonnull
    public static Color readColor(final ObjectInputStream stream) throws IOException {
        return new Color(stream.readDouble(), stream.readDouble(), stream.readDouble(), stream.readDouble());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
        final JsonObject jObj = json.getAsJsonObject();
        final double red = jObj.get(RED).getAsDouble();
        final double green = jObj.get(GREEN).getAsDouble();
        final double blue = jObj.get(BLUE).getAsDouble();
        final double alpha = jObj.get(ALPHA).getAsDouble();
        return new Color(red, green, blue, alpha);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement serialize(final Color src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jObj = new JsonObject();
        jObj.addProperty(RED, src.getRed());
        jObj.addProperty(GREEN, src.getGreen());
        jObj.addProperty(BLUE, src.getBlue());
        jObj.addProperty(ALPHA, src.getOpacity());
        return jObj;
    }
}
