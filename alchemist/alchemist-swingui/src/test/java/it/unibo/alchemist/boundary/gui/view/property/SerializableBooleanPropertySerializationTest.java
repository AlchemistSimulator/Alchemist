package it.unibo.alchemist.boundary.gui.view.property;

import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractPropertySerializationTest;
import it.unibo.alchemist.boundary.gui.view.properties.SerializableBooleanProperty;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test for custom {@link javafx.beans.property.Property} serialization.
 */
public final class SerializableBooleanPropertySerializationTest extends AbstractPropertySerializationTest {

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = TemporaryFile.create();
        assertTrue(file.createNewFile());
        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);
        final SerializableBooleanProperty serializableBooleanProperty = new SerializableBooleanProperty("Test", true);
        oos.writeObject(serializableBooleanProperty);
        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);
        final SerializableBooleanProperty deserialized = (SerializableBooleanProperty) ois.readObject();
        assertEquals(serializableBooleanProperty, deserialized, getMessage(serializableBooleanProperty, deserialized));
        final SerializableBooleanProperty serializableBooleanProperty2 = new SerializableBooleanProperty("Test2", false);
        oos.writeObject(serializableBooleanProperty2);
        final SerializableBooleanProperty deserialized2 = (SerializableBooleanProperty) ois.readObject();
        assertEquals(serializableBooleanProperty2, deserialized2, getMessage(serializableBooleanProperty2, deserialized2));
        assertNotEquals(serializableBooleanProperty, serializableBooleanProperty2);
        assertNotEquals(serializableBooleanProperty, deserialized2);
        assertNotEquals(deserialized, deserialized2);
        assertNotEquals(serializableBooleanProperty2, deserialized);
        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = TemporaryFile.create();
        assertTrue(file.createNewFile());
        final SerializableBooleanProperty serializableBooleanProperty = new SerializableBooleanProperty("Test", true);
        final Writer writer = new FileWriter(file);
        GSON.toJson(serializableBooleanProperty, this.getGsonType(), writer);
        writer.close();
        final Reader reader = new FileReader(file);
        final SerializableBooleanProperty deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();
        assertEquals(serializableBooleanProperty, deserialized, getMessage(serializableBooleanProperty, deserialized));
    }

    @Override
    protected Type getGsonType() {
        return new TypeToken<SerializableBooleanProperty>() { }.getType();
    }
}
