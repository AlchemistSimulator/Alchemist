package it.unibo.alchemist.boundary.gui.effects;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Abstract class that provides a common base of methods for effects
 * serialization test.
 * 
 * @param <T>
 *            the type of effect
 */
public abstract class AbstractEffectSerializationTest<T extends EffectFX> {

    /**
     * Temporary folder created before each test method, and deleted after each.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

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
     * Method that generate {@link Assert#assertTrue(boolean) assertTrue()}
     * messages.
     * 
     * @param origin
     *            the original {@link EffectFX effect}
     * @param deserialized
     *            the deserialized {@link EffectFX effect}
     * @return the message of test fail
     */
    protected String getMessage(final T origin, final T deserialized) {
        return "Effect \"" + origin.getName() + "\" is different from property \"" + deserialized.getName() + "\"";
    }
}
