package it.unibo.alchemist.boundary.gui.view.property;

import it.unibo.alchemist.boundary.gui.view.properties.SerializableEnumProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit test for custom {@link javafx.beans.property.Property} serialization.
 */
public class SerializableEnumPropertyTest {
    /**
     * Tests if {@link SerializableEnumProperty#values()} method works
     * correctly.
     */
    @Test
    public void testValues() {
        final SerializableEnumProperty<TestEnum> serializableEnumProperty = new SerializableEnumProperty<>("Test", TestEnum.FOO);
        assertArrayEquals(serializableEnumProperty.values(), TestEnum.values());
        final SerializableEnumProperty<TestEnum> enumProperty2 = new SerializableEnumProperty<>();
        try {
            assertArrayEquals(enumProperty2.values(), TestEnum.values());
            fail("Exception not thrown");
        } catch (final IllegalStateException e) {
            enumProperty2.setValue(TestEnum.BAR);
            assertArrayEquals(enumProperty2.values(), TestEnum.values());
        }
    }

    /**
     * Enum with the only purpose of test {@link SerializableEnumProperty}.
     */
    private enum TestEnum {
        FOO,
        BAR,
        TEST
    }

}
