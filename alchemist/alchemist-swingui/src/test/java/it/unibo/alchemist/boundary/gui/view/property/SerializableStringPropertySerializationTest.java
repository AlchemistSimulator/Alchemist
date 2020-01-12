package it.unibo.alchemist.boundary.gui.view.property;

import com.google.common.base.Charsets;
import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractPropertySerializationTest;
import it.unibo.alchemist.boundary.gui.view.properties.SerializableStringProperty;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test for custom {@link javafx.beans.property.Property} serialization.
 */
public final class SerializableStringPropertySerializationTest extends AbstractPropertySerializationTest {

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = TemporaryFile.create();
        SerializableStringProperty serializableStringProperty;
        SerializableStringProperty deserialized;
        try (
                FileOutputStream fout = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                FileInputStream fin = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fin)
                ) {
            serializableStringProperty = new SerializableStringProperty("Pippo", "Test string");
            oos.writeObject(serializableStringProperty);
            deserialized = (SerializableStringProperty) ois.readObject();
        }
        assertEquals(serializableStringProperty, deserialized, getMessage(serializableStringProperty, deserialized));
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = TemporaryFile.create();
        final SerializableStringProperty serializableStringProperty = new SerializableStringProperty("Pippo", "Test string");
        SerializableStringProperty deserialized;
        try (
                Writer writer = new FileWriter(file, Charsets.UTF_8);
                Reader reader = new FileReader(file, Charsets.UTF_8)
                ) {
            GSON.toJson(serializableStringProperty, this.getGsonType(), writer);
            deserialized = GSON.fromJson(reader, this.getGsonType());
        }
        assertEquals(serializableStringProperty, deserialized, getMessage(serializableStringProperty, deserialized));
    }

    @Override
    protected Type getGsonType() {
        return new TypeToken<SerializableStringProperty>() { }.getType();
    }

}
