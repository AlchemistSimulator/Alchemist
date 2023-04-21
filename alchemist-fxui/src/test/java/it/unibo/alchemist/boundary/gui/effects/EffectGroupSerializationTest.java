package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.fxui.effects.api.EffectGroup;
import it.unibo.alchemist.boundary.fxui.effects.impl.DrawColoredDot;
import it.unibo.alchemist.boundary.fxui.effects.impl.DrawDot;
import it.unibo.alchemist.boundary.fxui.effects.impl.EffectStack;
import it.unibo.alchemist.boundary.fxui.effects.serialization.impl.EffectSerializer;
import it.unibo.alchemist.model.Position2D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test for {@link EffectGroup} and {@link EffectStack} serialization.
 */
class EffectGroupSerializationTest {
    private static final double TEST_DOT_SIZE = 22.0;
    private static final double TEST_COLORED_DOT_SIZE = 25.0;

    /**
     * Tests (de)serialization with default Java serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    void testJavaSerialization() throws Exception {
        final File file = File.createTempFile("testJavaSerialization", null);
        final var effects = this.setupEffectGroup();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(effects);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            final var deserialized = (EffectStack<?>) ois.readObject();
            assertEquals(effects, deserialized, getMessage(effects, deserialized));
        }
    }

    /**
     * Tests (de)serialization with Google Gson serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    void testGsonSerialization() throws Exception {
        final File file = File.createTempFile("testGsonSerialization", null);
        final var effect = this.setupEffectGroup();
        EffectSerializer.effectsToFile(file, effect);
        final var deserialized = EffectSerializer.effectsFromFile(file);
        assertEquals(effect, deserialized, getMessage(effect, deserialized));
    }

    /**
     * Method that generate {@link org.junit.jupiter.api.Assertions#assertTrue(boolean) assertTrue()}
     * messages.
     * 
     * @param origin
     *            the original {@link it.unibo.alchemist.boundary.fxui.effects.api.EffectFX effect}
     * @param deserialized
     *            the deserialized {@link it.unibo.alchemist.boundary.fxui.effects.api.EffectFX effect}
     * @return the message of test fail
     */
    protected String getMessage(final EffectGroup<?> origin, final EffectGroup<?> deserialized) {
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
     * @param <P> position type
     * @return the effect group
     */
    private <P extends Position2D<? extends P>> EffectGroup<P> setupEffectGroup() {
        final EffectGroup<P> effects = new EffectStack<>("TestGroup");
        effects.add(new DrawDot<>("TestDot"));
        final var dot = new DrawDot<P>();
        dot.setSize(TEST_DOT_SIZE);
        effects.add(dot);
        final var coloredDot = new DrawColoredDot<P>("Colored Dot");
        coloredDot.setSize(TEST_COLORED_DOT_SIZE);
        coloredDot.setColor(Color.CYAN);
        effects.add(coloredDot);
        return effects;
    }

}
