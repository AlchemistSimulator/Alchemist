package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.fxui.effects.DrawDot;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractEffectSerializationTest;
import it.unibo.alchemist.boundary.fxui.effects.serialization.EffectSerializer;
import it.unibo.alchemist.test.TemporaryFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUint test for {@link DrawDot} effect serialization.
 */
final class DrawDotSerializationTest extends AbstractEffectSerializationTest<DrawDot<?>> {
    private static final String TEST_NAME = "TestDot";
    private static final double TEST_SIZE = 22.0;

    @Test
    void testJavaSerialization() throws IOException, ClassNotFoundException {
        final var effect = new DrawDot<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        testSerializationOf(effect);
    }

    @Test
    void testGsonSerialization() throws IOException {
        final File file = TemporaryFile.create();
        final var effect = new DrawDot<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        EffectSerializer.effectToFile(file, effect);
        final var deserialized = (DrawDot<?>) EffectSerializer.effectFromFile(file);
        assertEquals(effect, deserialized, getMessage(effect, deserialized));
    }

    @Override
    protected String getMessage(final DrawDot<?> origin, final DrawDot<?> deserialized) {
        if (origin == null || deserialized == null) {
            return super.getMessage(origin, deserialized);
        }

        return super.getMessage(origin, deserialized)
                + System.lineSeparator() + "Origin size: " + origin.getSize()
                + System.lineSeparator() + "Deserialized size: " + deserialized.getSize();
    }

}
