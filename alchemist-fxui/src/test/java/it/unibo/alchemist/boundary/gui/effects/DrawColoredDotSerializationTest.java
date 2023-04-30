package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.fxui.effects.DrawColoredDot;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractEffectSerializationTest;
import it.unibo.alchemist.boundary.fxui.effects.serialization.EffectSerializer;
import it.unibo.alchemist.test.TemporaryFile;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test for {@link DrawColoredDot} effect serialization.
 */
final class DrawColoredDotSerializationTest extends AbstractEffectSerializationTest<DrawColoredDot<?>> {
    private static final String TEST_NAME = "TestDot";
    private static final double TEST_SIZE = 25.0;
    private static final Color TEST_COLOR = Color.CYAN;

    @Test
    void testJavaSerialization() throws IOException, ClassNotFoundException {
        final var effect = new DrawColoredDot<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        effect.setColor(TEST_COLOR);
        testSerializationOf(effect);
    }

    @Test
    void testGsonSerialization() throws IOException {
        final File file = TemporaryFile.create();
        final var effect = new DrawColoredDot<>(TEST_NAME);
        effect.setSize(TEST_SIZE);
        effect.setColor(TEST_COLOR);
        EffectSerializer.effectToFile(file, effect);
        final var deserialized = (DrawColoredDot<?>) EffectSerializer.effectFromFile(file);
        assertEquals(effect, deserialized, getMessage(effect, deserialized));
    }

    @Override
    protected String getMessage(final DrawColoredDot<?> origin, final DrawColoredDot<?> deserialized) {
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
