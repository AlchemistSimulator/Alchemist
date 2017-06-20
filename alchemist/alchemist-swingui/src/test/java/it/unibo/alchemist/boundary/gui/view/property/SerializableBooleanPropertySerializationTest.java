package it.unibo.alchemist.boundary.gui.view.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.view.properties.SerializableBooleanProperty;
import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class SerializableBooleanPropertySerializationTest extends AbstractPropertySerializationTest {

    /**
     * Tests if the {@link SerializableBooleanProperty} is serialized correctly.
     * 
     * @throws IOException
     *             if I/O problems occur
     * @throws ClassNotFoundException
     *             if a problem occurs during deserialization
     */
    @Test
    @Override
    public void test() throws IOException, ClassNotFoundException {
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

}
