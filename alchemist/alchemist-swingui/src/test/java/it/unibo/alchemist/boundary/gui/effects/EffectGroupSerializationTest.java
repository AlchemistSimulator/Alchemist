package it.unibo.alchemist.boundary.gui.effects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;

/**
 * JUnit test for {@link EffectGroup} and {@link EffectStack} serialization.
 */
public class EffectGroupSerializationTest {
    /** Temporary folder created before each test method, and deleted after each. */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Tests (de)serialization with default Java serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public void testJavaSerialization() throws Exception {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final EffectGroup effects = this.setupEffectGroup();

        oos.writeObject(effects);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final EffectGroup deserialized = (EffectStack) ois.readObject();

        Assert.assertTrue(getMessage(effects, deserialized), effects.equals(deserialized));

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
        final File file = folder.newFile();

        final EffectGroup effect = this.setupEffectGroup();

        EffectSerializer.effectsToFile(file, effect);

        final EffectGroup deserialized = EffectSerializer.effectsFromFile(file);

        Assert.assertTrue(getMessage(effect, deserialized), effect.equals(deserialized));
    }

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
        // CHECKSTYLE:OFF
        dot.setSize(22.0);
        // CHECKSTYLE:ON
        effects.add(dot);
        final DrawColoredDot coloredDot = new DrawColoredDot("Colored Dot");
        // CHECKSTYLE:OFF
        coloredDot.setSize(25.0);
        // CHECKSTYLE:ON
        coloredDot.setColor(Color.CYAN);
        effects.add(coloredDot);
        effects.add(new DrawShapeFX());
        return effects;
    }

}
