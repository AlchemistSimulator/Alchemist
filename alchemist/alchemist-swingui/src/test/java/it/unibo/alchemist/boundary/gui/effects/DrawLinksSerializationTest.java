package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.effects.json.AbstractEffectSerializationTest;
import it.unibo.alchemist.boundary.gui.effects.json.EffectSerializer;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.test.TemporaryFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test for {@link DrawLinks} effect serialization.
 */
public final class DrawLinksSerializationTest extends AbstractEffectSerializationTest<DrawLinks<?>> {
    private static final String TEST_NAME = "TestLinks";
    private static final double TEST_SIZE = 12.0;

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = TemporaryFile.create();
        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);
        final var effect = new DrawLinks<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        oos.writeObject(effect);
        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);
        final DrawLinks<?> deserialized = (DrawLinks<?>) ois.readObject();
        assertEquals(effect, deserialized, getMessage(effect, deserialized));
        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws IOException {
        final File file = TemporaryFile.create();
        final DrawLinks<Position2D<? extends Position2D<?>>> effect = new DrawLinks<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        EffectSerializer.effectToFile(file, effect);
        final DrawLinks<Position2D<? extends Position2D<?>>> deserialized = (DrawLinks<Position2D<? extends Position2D<?>>>) EffectSerializer.effectFromFile(file);
        assertEquals(effect, deserialized, getMessage(effect, deserialized));
    }

    @Override
    protected String getMessage(final DrawLinks<?> origin, final DrawLinks<?> deserialized) {
        if (origin == null || deserialized == null) {
            return super.getMessage(origin, deserialized);
        }

        return super.getMessage(origin, deserialized)
                + System.lineSeparator() + "Origin size: " + origin.getSize()
                + System.lineSeparator() + "Deserialized size: " + deserialized.getSize();
    }
}
