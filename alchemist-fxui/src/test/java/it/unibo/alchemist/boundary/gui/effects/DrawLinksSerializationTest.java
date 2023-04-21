package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.fxui.effects.impl.DrawLinks;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractEffectSerializationTest;
import it.unibo.alchemist.boundary.fxui.effects.serialization.impl.EffectSerializer;
import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.test.TemporaryFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test for {@link DrawLinks} effect serialization.
 */
final class DrawLinksSerializationTest extends AbstractEffectSerializationTest<DrawLinks<?>> {
    private static final String TEST_NAME = "TestLinks";
    private static final double TEST_SIZE = 12.0;

    @Test
    void testJavaSerialization() throws IOException, ClassNotFoundException {
        final var effect = new DrawLinks<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        testSerializationOf(effect);
    }

    @Test
    void testGsonSerialization() throws IOException {
        final File file = TemporaryFile.create();
        final DrawLinks<Position2D<? extends Position2D<?>>> effect = new DrawLinks<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        EffectSerializer.effectToFile(file, effect);
        final DrawLinks<Position2D<? extends Position2D<?>>> deserialized =
                (DrawLinks<Position2D<? extends Position2D<?>>>) EffectSerializer.effectFromFile(file);
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
