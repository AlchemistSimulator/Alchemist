package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.fxui.effects.AbstractEffect;
import it.unibo.alchemist.boundary.fxui.effects.DrawColoredDot;
import it.unibo.alchemist.boundary.fxui.effects.DrawDot;
import it.unibo.alchemist.boundary.fxui.effects.DrawLinks;
import it.unibo.alchemist.boundary.fxui.properties.SerializableStringProperty;
import org.junit.jupiter.api.Test;

import static it.unibo.alchemist.boundary.fxui.effects.AbstractEffect.checkBasicProperties;
import static it.unibo.alchemist.boundary.fxui.effects.AbstractEffect.checkEqualsProperties;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link AbstractEffect} static comparison methods.
 */
class AbstractEffectTest {
    private static final String TEST_NAME = "Test";

    /**
     * Test AbstractEffect.checkBasicProperties(AbstractEffect, Object).
     */
    @Test
    void testCheckBasicProperties() {
        final var dot1 = new DrawDot<>();
        final var dot2 = new DrawDot<>(TEST_NAME);
        final var dot3 = new DrawDot<>();
        assertFalse(checkBasicProperties(null, dot1));
        assertFalse(checkBasicProperties(dot2, null));
        assertTrue(checkBasicProperties(dot1, dot3));
        assertFalse(checkBasicProperties(dot2, dot3));
        final var coloredDot1 = new DrawColoredDot<>();
        final var coloredDot2 = new DrawColoredDot<>(TEST_NAME);
        final var coloredDot3 = new DrawColoredDot<>();
        assertFalse(checkBasicProperties(coloredDot1, null));
        assertTrue(checkBasicProperties(dot2, coloredDot2));
        assertTrue(checkBasicProperties(coloredDot3, coloredDot1));
        assertFalse(checkBasicProperties(null, coloredDot2));
        final var links1 = new DrawLinks<>();
        final var links2 = new DrawLinks<>(TEST_NAME);
        final var links3 = new DrawLinks<>();
        assertFalse(checkBasicProperties(links1, null));
        assertTrue(checkBasicProperties(links2, links2));
        assertTrue(checkBasicProperties(links3, links1));
        assertFalse(checkBasicProperties(null, links2));
        assertFalse(checkBasicProperties(coloredDot1, links2));
        assertFalse(checkBasicProperties(coloredDot2, links2));
    }

    /**
     * Test method to test
     * {@link AbstractEffect#checkEqualsProperties(javafx.beans.property.Property, javafx.beans.property.Property)}
     * method.
     */
    @Test
    void testCheckEqualsProperties() {
        final SerializableStringProperty string1 = new SerializableStringProperty(TEST_NAME, TEST_NAME);
        final SerializableStringProperty string2 = new SerializableStringProperty();
        final SerializableStringProperty string3 = new SerializableStringProperty();
        assertFalse(checkEqualsProperties(string1, null));
        assertFalse(checkEqualsProperties(string1, string2));
        assertTrue(checkEqualsProperties(string2, string3));
        string3.setName(TEST_NAME);
        assertFalse(checkEqualsProperties(string1, string3));
        string3.setValue(TEST_NAME);
        assertTrue(checkEqualsProperties(string1, string3));
    }
}
