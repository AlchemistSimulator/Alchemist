package it.unibo.alchemist.boundary.gui.view.property;

import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractPropertySerializationTest;
import it.unibo.alchemist.boundary.gui.view.properties.SerializableEnumProperty;
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
import javafx.beans.property.Property;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class SerializableEnumPropertySerializationTest extends AbstractPropertySerializationTest {
    /**
     * Tests if {@link SerializableEnumProperty#values()} method works
     * correctly.
     */
    @Test
    public void testValues() {
        final SerializableEnumProperty<TestEnum> serializableEnumProperty = new SerializableEnumProperty<>("Test", TestEnum.FOO);

        Assert.assertArrayEquals(serializableEnumProperty.values(), TestEnum.values());

        final SerializableEnumProperty<TestEnum> enumProperty2 = new SerializableEnumProperty<>();

        try {
            Assert.assertArrayEquals(enumProperty2.values(), TestEnum.values());
            Assert.fail("Exception not thrown");
        } catch (final IllegalStateException e) {
            enumProperty2.setValue(TestEnum.BAR);
            Assert.assertArrayEquals(enumProperty2.values(), TestEnum.values());
        }
    }

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final SerializableEnumProperty<TestEnum> serializableEnumProperty = new SerializableEnumProperty<>("Test", TestEnum.TEST);

        oos.writeObject(serializableEnumProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        @SuppressWarnings("unchecked") // If something goes wrong, the test will fail
        final SerializableEnumProperty<TestEnum> deserialized = (SerializableEnumProperty<TestEnum>) ois.readObject();

        Assert.assertTrue(getMessage(serializableEnumProperty, deserialized), serializableEnumProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = folder.newFile();

        final SerializableEnumProperty<TestEnum> serializableEnumProperty = new SerializableEnumProperty<>("Test", TestEnum.FOO);

        final Writer writer = new FileWriter(file);
        GSON.toJson(serializableEnumProperty, this.getGsonType(), writer);
        writer.close();

        final Reader reader = new FileReader(file);
        final SerializableEnumProperty<TestEnum> deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();

        Assert.assertTrue(getMessage(serializableEnumProperty, deserialized), serializableEnumProperty.equals(deserialized));
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
