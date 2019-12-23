package it.unibo.alchemist.boundary.gui.effects;

import it.unibo.alchemist.boundary.gui.view.properties.SerializableBooleanProperty;
import it.unibo.alchemist.boundary.gui.view.properties.SerializableStringProperty;
import javafx.beans.property.Property;
import org.junit.jupiter.api.Test;

import static it.unibo.alchemist.boundary.gui.effects.AbstractEffect.checkBasicProperties;
import static it.unibo.alchemist.boundary.gui.effects.AbstractEffect.checkEqualsProperties;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link AbstractEffect} static comparison methods.
 */
public class AbstractEffectTest {
    private static final String TEST_NAME = "Test";

    /**
     * Test method to test {@link AbstractEffect#checkBasicProperties(AbstractEffect, Object)} method.
     */
    @Test
    public void testCheckBasicProperties() {
        final DrawDot nullDot = null;
        final DrawColoredDot nullColoredDot = null;
        final DrawLinks nullLinks = null;
        assertTrue(checkBasicProperties(nullDot, nullLinks));
        assertTrue(checkBasicProperties(nullDot, nullColoredDot));
        assertTrue(checkBasicProperties(nullColoredDot, nullLinks));
        final DrawDot dot1 = new DrawDot();
        final DrawDot dot2 = new DrawDot(TEST_NAME);
        final DrawDot dot3 = new DrawDot();
        assertFalse(checkBasicProperties(nullDot, dot1));
        assertFalse(checkBasicProperties(dot2, nullDot));
        assertTrue(checkBasicProperties(dot1, dot3));
        assertFalse(checkBasicProperties(dot2, dot3));
        final DrawColoredDot coloredDot1 = new DrawColoredDot();
        final DrawColoredDot coloredDot2 = new DrawColoredDot(TEST_NAME);
        final DrawColoredDot coloredDot3 = new DrawColoredDot();
        assertFalse(checkBasicProperties(coloredDot1, nullColoredDot));
        assertTrue(checkBasicProperties(dot2, coloredDot2));
        assertTrue(checkBasicProperties(coloredDot3, coloredDot1));
        assertFalse(checkBasicProperties(nullColoredDot, coloredDot2));
        final DrawLinks links1 = new DrawLinks();
        final DrawLinks links2 = new DrawLinks(TEST_NAME);
        final DrawLinks links3 = new DrawLinks();
        assertFalse(checkBasicProperties(links1, nullLinks));
        assertTrue(checkBasicProperties(links2, links2));
        assertTrue(checkBasicProperties(links3, links1));
        assertFalse(checkBasicProperties(nullLinks, links2));
        assertFalse(checkBasicProperties(coloredDot1, links2));
        assertFalse(checkBasicProperties(coloredDot2, links2));
    }

    /**
     * Test method to test {@link AbstractEffect#checkEqualsProperties(Property, Property)} method.
     */
    @Test
    public void testCheckEqualsProperties() {
        final SerializableBooleanProperty nullBoolean = null;
        final SerializableStringProperty nullString = null;
        assertTrue(checkEqualsProperties(null, nullBoolean));
        assertTrue(checkEqualsProperties(nullString, null));
        final SerializableStringProperty string1 = new SerializableStringProperty(TEST_NAME, TEST_NAME);
        final SerializableStringProperty string2 = new SerializableStringProperty();
        final SerializableStringProperty string3 = new SerializableStringProperty();
        assertFalse(checkEqualsProperties(string1, nullString));
        assertFalse(checkEqualsProperties(string1, string2));
        assertTrue(checkEqualsProperties(string2, string3));
        string3.setName(TEST_NAME);
        assertFalse(checkEqualsProperties(string1, string3));
        string3.setValue(TEST_NAME);
        assertTrue(checkEqualsProperties(string1, string3));
    }
}
