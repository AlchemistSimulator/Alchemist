package it.unibo.alchemist.boundary.gui.effects.json;

import com.google.gson.*;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

/**
 * This class should be registered in a {@link GsonBuilder} to
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
     * @throws EOFException           if the end of file is reached
     * @throws ClassNotFoundException if cannot find the class
     * @throws IOException            if other I/O error has occurred
     */
    @NotNull
    public static Color readColor(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        return new Color(stream.readDouble(), stream.readDouble(), stream.readDouble(), stream.readDouble());
    }

    @Override
    public Color deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jObj = json.getAsJsonObject();

        final double red = jObj.get(RED).getAsDouble();
        final double green = jObj.get(GREEN).getAsDouble();
        final double blue = jObj.get(BLUE).getAsDouble();
        final double alpha = jObj.get(ALPHA).getAsDouble();

        return new Color(red, green, blue, alpha);
    }

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
