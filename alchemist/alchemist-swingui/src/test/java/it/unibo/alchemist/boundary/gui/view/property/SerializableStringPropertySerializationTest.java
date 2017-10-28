package it.unibo.alchemist.boundary.gui.view.property;

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

import it.unibo.alchemist.boundary.gui.effects.json.AbstractPropertySerializationTest;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import it.unibo.alchemist.boundary.gui.view.properties.SerializableStringProperty;
import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class SerializableStringPropertySerializationTest extends AbstractPropertySerializationTest {

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final SerializableStringProperty serializableStringProperty = new SerializableStringProperty("Pippo", "Test string");

        oos.writeObject(serializableStringProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final SerializableStringProperty deserialized = (SerializableStringProperty) ois.readObject();

        Assert.assertTrue(getMessage(serializableStringProperty, deserialized), serializableStringProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = folder.newFile();

        final SerializableStringProperty serializableStringProperty = new SerializableStringProperty("Pippo", "Test string");

        final Writer writer = new FileWriter(file);
        GSON.toJson(serializableStringProperty, this.getGsonType(), writer);
        writer.close();

        final Reader reader = new FileReader(file);
        final SerializableStringProperty deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();

        Assert.assertTrue(getMessage(serializableStringProperty, deserialized), serializableStringProperty.equals(deserialized));
    }

    @Override
    protected Type getGsonType() {
        return new TypeToken<SerializableStringProperty>() { }.getType();
    }

}
