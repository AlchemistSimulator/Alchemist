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

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import it.unibo.alchemist.boundary.gui.view.properties.SerializableBooleanProperty;
import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class SerializableBooleanPropertySerializationTest extends AbstractPropertySerializationTest {

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final SerializableBooleanProperty serializableBooleanProperty = new SerializableBooleanProperty("Test", true);

        oos.writeObject(serializableBooleanProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final SerializableBooleanProperty deserialized = (SerializableBooleanProperty) ois.readObject();

        Assert.assertTrue(getMessage(serializableBooleanProperty, deserialized), serializableBooleanProperty.equals(deserialized));

        final SerializableBooleanProperty serializableBooleanProperty2 = new SerializableBooleanProperty("Test2", false);

        oos.writeObject(serializableBooleanProperty2);

        final SerializableBooleanProperty deserialized2 = (SerializableBooleanProperty) ois.readObject();

        Assert.assertTrue(getMessage(serializableBooleanProperty2, deserialized2), serializableBooleanProperty2.equals(deserialized2));
        Assert.assertFalse(serializableBooleanProperty.equals(serializableBooleanProperty2));
        Assert.assertFalse(serializableBooleanProperty.equals(deserialized2));
        Assert.assertFalse(deserialized.equals(deserialized2));
        Assert.assertFalse(serializableBooleanProperty2.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = folder.newFile();

        final SerializableBooleanProperty serializableBooleanProperty = new SerializableBooleanProperty("Test", true);

        final Writer writer = new FileWriter(file);
        GSON.toJson(serializableBooleanProperty, this.getGsonType(), writer);
        writer.close();

        final Reader reader = new FileReader(file);
        final SerializableBooleanProperty deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();

        Assert.assertTrue(getMessage(serializableBooleanProperty, deserialized), serializableBooleanProperty.equals(deserialized));
    }

    @Override
    protected Type getGsonType() {
        return new TypeToken<SerializableBooleanProperty>() { }.getType();
    }

}
