package it.unibo.alchemist.boundary.gui.effects.json;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * Abstract class that provides a common base of methods for effects
 * serialization test.
 * 
 * @param <T>
 *            the type of effect
 */
public abstract class AbstractEffectSerializationTest<T extends EffectFX<?>> {

    /**
     * Tests (de)serialization with default Java serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public abstract void testJavaSerialization() throws Exception;

    /**
     * Tests (de)serialization with Google Gson serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public abstract void testGsonSerialization() throws Exception;

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
}
