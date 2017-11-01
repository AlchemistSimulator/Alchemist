package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.effects.json.AbstractEffectSerializationTest;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

/**
 * JUint test for {@link DrawLinks} effect serialization.
 */
public class DrawLinksSerializationTest extends AbstractEffectSerializationTest<DrawLinks> {
    private static final String TEST_NAME = "TestLinks";
    private static final double TEST_SIZE = 12.0;

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final DrawLinks effect = new DrawLinks(TEST_NAME);
        effect.setSize(TEST_SIZE);
        oos.writeObject(effect);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final DrawLinks deserialized = (DrawLinks) ois.readObject();

        Assert.assertTrue(getMessage(effect, deserialized), effect.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws IOException {
        final File file = folder.newFile();

        final DrawLinks effect = new DrawLinks(TEST_NAME);
        effect.setSize(TEST_SIZE);
        EffectSerializer.effectToFile(file, effect);

        final DrawLinks deserialized = (DrawLinks) EffectSerializer.effectFromFile(file);

        Assert.assertTrue(getMessage(effect, deserialized), effect.equals(deserialized));
    }

    @Override
    protected String getMessage(final DrawLinks origin, final DrawLinks deserialized) {
        if (origin == null || deserialized == null) {
            return super.getMessage(origin, deserialized);
        }

        return super.getMessage(origin, deserialized)
                + System.lineSeparator() + "Origin size: " + origin.getSize()
                + System.lineSeparator() + "Deserialized size: " + deserialized.getSize();
    }
}
