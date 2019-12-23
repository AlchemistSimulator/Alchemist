package it.unibo.alchemist.boundary.gui.effects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.scene.paint.Color;

import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test for {@link EffectGroup} and {@link EffectStack} serialization.
 */
public class EffectGroupSerializationTest {
    private static final double TEST_DOT_SIZE = 22.0;
    private static final double TEST_COLORED_DOT_SIZE = 25.0;
    /** Temporary folder created before each test method, and deleted after each. */

    /**
     * Tests (de)serialization with default Java serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public void testJavaSerialization() throws Exception {
        final File file = File.createTempFile("testJavaSerialization", null);
        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);
        final EffectGroup effects = this.setupEffectGroup();
        oos.writeObject(effects);
        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);
        final EffectGroup deserialized = (EffectStack) ois.readObject();
        assertTrue(effects.equals(deserialized), getMessage(effects, deserialized));
        oos.close();
        ois.close();
    }

    /**
     * Tests (de)serialization with Google Gson serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public void testGsonSerialization() throws Exception {
        final File file = File.createTempFile("testGsonSerialization", null);
        final EffectGroup effect = this.setupEffectGroup();
        EffectSerializer.effectsToFile(file, effect);
        final EffectGroup deserialized = EffectSerializer.effectsFromFile(file);
        assertTrue(effect.equals(deserialized), getMessage(effect, deserialized));
    }

    /**
     * Method that generate {@link org.junit.jupiter.api.Assertions#assertTrue(boolean) assertTrue()}
     * messages.
     * 
     * @param origin
     *            the original {@link EffectFX effect}
     * @param deserialized
     *            the deserialized {@link EffectFX effect}
     * @return the message of test fail
     */
    protected String getMessage(final EffectGroup origin, final EffectGroup deserialized) {
        if (origin == null) {
            return "Original group is null";
        }

        if (deserialized == null) {
            return "Deserialized group is null";
        }

        return "Effect group\"" + origin.getName() + "\" is different from group \"" + deserialized.getName() + "\"";
    }

    /**
     * Initializes and returns a simple {@link EffectStack}.
     * 
     * @return the effect group
     */
    private EffectGroup setupEffectGroup() {
        final EffectGroup effects = new EffectStack("TestGroup");
        effects.add(new DrawDot("TestDot"));
        final DrawDot dot = new DrawDot();
        dot.setSize(TEST_DOT_SIZE);
        effects.add(dot);
        final DrawColoredDot coloredDot = new DrawColoredDot("Colored Dot");
        coloredDot.setSize(TEST_COLORED_DOT_SIZE);
        coloredDot.setColor(Color.CYAN);
        effects.add(coloredDot);
        return effects;
    }

}
