package it.unibo.alchemist.boundary.gui.effects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.effects.json.AbstractEffectSerializationTest;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import javafx.scene.paint.Color;

/**
 * JUint test for {@link DrawColoredDot} effect serialization.
 */
public class DrawColoredDotSerializationTest extends AbstractEffectSerializationTest<DrawColoredDot> {
    private static final String TEST_NAME = "TestDot";
    private static final double TEST_SIZE = 25.0;
    private static final Color TEST_COLOR = Color.CYAN;

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final DrawColoredDot effect = new DrawColoredDot(TEST_NAME);
        effect.setSize(TEST_SIZE);
        effect.setColor(TEST_COLOR);

        oos.writeObject(effect);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final DrawColoredDot deserialized = (DrawColoredDot) ois.readObject();

        Assert.assertTrue(getMessage(effect, deserialized), effect.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws IOException {
        final File file = folder.newFile();

        final DrawColoredDot effect = new DrawColoredDot(TEST_NAME);
        effect.setSize(TEST_SIZE);
        effect.setColor(TEST_COLOR);

        EffectSerializer.effectToFile(file, effect);

        final DrawColoredDot deserialized = (DrawColoredDot) EffectSerializer.effectFromFile(file);

        Assert.assertTrue(getMessage(effect, deserialized), effect.equals(deserialized));
    }

    @Override
    protected String getMessage(final DrawColoredDot origin, final DrawColoredDot deserialized) {
        if (origin == null || deserialized == null) {
            return super.getMessage(origin, deserialized);
        }

        return super.getMessage(origin, deserialized)
                + System.lineSeparator() + "Origin size: " + origin.getSize()
                + System.lineSeparator() + "Deserialized size: " + deserialized.getSize()
                + System.lineSeparator() + "Origin alpha: " + origin.getAlpha()
                + System.lineSeparator() + "Deserialized alpha: " + deserialized.getAlpha()
                + System.lineSeparator() + "Origin red: " + origin.getRed()
                + System.lineSeparator() + "Deserialized red: " + deserialized.getRed()
                + System.lineSeparator() + "Origin green: " + origin.getGreen()
                + System.lineSeparator() + "Deserialized green: " + deserialized.getGreen()
                + System.lineSeparator() + "Origin blue: " + origin.getBlue()
                + System.lineSeparator() + "Deserialized blue: " + deserialized.getBlue();
    }
}
