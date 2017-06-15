package it.unibo.alchemist.boundary.gui.view.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class SerializableStringPropertySerializationTest extends AbstractPropertySerializationTest {

    /**
     * Tests if the {@link SerializableStringProperty} is serialized correctly.
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

        final SerializableStringProperty serializableStringProperty = new SerializableStringProperty("Pippo", "Test string");

        oos.writeObject(serializableStringProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final SerializableStringProperty deserialized = (SerializableStringProperty) ois.readObject();

        Assert.assertTrue(getMessage(serializableStringProperty, deserialized), serializableStringProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

}
