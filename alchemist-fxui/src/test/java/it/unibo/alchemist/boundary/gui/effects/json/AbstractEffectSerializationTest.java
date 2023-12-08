package it.unibo.alchemist.boundary.gui.effects.json;

import it.unibo.alchemist.boundary.fxui.EffectFX;
import it.unibo.alchemist.test.TemporaryFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract class that provides a common base of methods for effects
 * serialization test.
 * 
 * @param <T>
 *            the type of effect
 */
public class AbstractEffectSerializationTest<T extends EffectFX<?>> {

    /**
     * Method that generate {@link org.junit.jupiter.api.Assertions#assertTrue(boolean)}
     * messages.
     * 
     * @param origin
     *            the original {@link EffectFX effect}
     * @param deserialized
     *            the deserialized {@link EffectFX effect}
     * @return the message of test fail
     */
    protected String getMessage(final T origin, final T deserialized) {
        if (origin == null) {
            return "Original Effect is null";
        }
        if (deserialized == null) {
            return "Deserialized Effect is null";
        }
        return "Effect \"" + origin.getName() + "\" is different from effect \"" + deserialized.getName() + "\"";
    }

    /**
     *
     * Serializes an effect, reloads it, and verifies that the serialized version is equal to the original one.
     *
     * @param effect the effect to serialize
     * @throws IOException in case of I/O errors
     * @throws ClassNotFoundException this should never happen
     */
    protected final void testSerializationOf(final T effect) throws IOException, ClassNotFoundException {
        final File file = TemporaryFile.create();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(effect);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked") final var deserialized = (T) ois.readObject();
            assertEquals(effect, deserialized, getMessage(effect, deserialized));
        }
    }
}
