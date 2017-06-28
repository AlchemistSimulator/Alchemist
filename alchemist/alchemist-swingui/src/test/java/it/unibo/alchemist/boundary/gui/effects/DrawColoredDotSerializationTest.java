package it.unibo.alchemist.boundary.gui.effects;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;

/**
 * JUint test for {@link EffectFX effect} serialization.
 */
public class DrawColoredDotSerializationTest extends AbstractEffectSerializationTest<DrawColoredDot> {

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final DrawColoredDot effect = new DrawColoredDot("TestDot");
        // CHECKSTYLE:OFF
        effect.setSize(25.0);
        // CHECKSTYLE:ON
        effect.setColor(Color.cyan);

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

        final DrawColoredDot effect = new DrawColoredDot("TestDot");
        // CHECKSTYLE:OFF
        effect.setSize(25.0);
        // CHECKSTYLE:ON
        effect.setColor(Color.cyan);

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
