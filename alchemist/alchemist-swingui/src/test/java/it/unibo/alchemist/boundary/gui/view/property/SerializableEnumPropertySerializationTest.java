package it.unibo.alchemist.boundary.gui.view.property;

import com.google.common.base.Charsets;
import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractPropertySerializationTest;
import it.unibo.alchemist.boundary.gui.view.properties.SerializableEnumProperty;
import it.unibo.alchemist.test.TemporaryFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit test for custom {@link javafx.beans.property.Property} serialization.
 */
public class SerializableEnumPropertySerializationTest extends AbstractPropertySerializationTest {
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

    @Test
    @Override
    @SuppressWarnings("unchecked") // If something goes wrong, the test will fail
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = TemporaryFile.create();
        SerializableEnumProperty<TestEnum> serializableEnumProperty;
        SerializableEnumProperty<TestEnum> deserialized;
        try (
                FileOutputStream fout = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                FileInputStream fin = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fin)
        ) {
            serializableEnumProperty = new SerializableEnumProperty<>("Test", TestEnum.TEST);
            oos.writeObject(serializableEnumProperty);
            deserialized = (SerializableEnumProperty<TestEnum>) ois.readObject();
        }
        assertEquals(serializableEnumProperty, deserialized, getMessage(serializableEnumProperty, deserialized));
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = TemporaryFile.create();
        final SerializableEnumProperty<TestEnum> serializableEnumProperty = new SerializableEnumProperty<>("Test", TestEnum.FOO);
        SerializableEnumProperty<TestEnum> deserialized;
        try (
                Writer writer = new FileWriter(file, Charsets.UTF_8);
                Reader reader = new FileReader(file, Charsets.UTF_8)
                ) {
            GSON.toJson(serializableEnumProperty, this.getGsonType(), writer);
            deserialized = GSON.fromJson(reader, this.getGsonType());
        }
        assertEquals(serializableEnumProperty, deserialized, getMessage(serializableEnumProperty, deserialized));
    }

    /**
     * {@inheritDoc}
     *
     * @return a {@link Type} for
     * {@link SerializableEnumProperty}<{@link TestEnum}>
     */
    @Override
    protected Type getGsonType() {
        return new TypeToken<SerializableEnumProperty<TestEnum>>() { }.getType();
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
