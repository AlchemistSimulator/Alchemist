package it.unibo.alchemist.boundary.gui.view.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.effects.DrawShapeFX.ModeFX;
import it.unibo.alchemist.boundary.gui.view.properties.SerializableEnumProperty;
import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class SerializableEnumPropertySerializationTest extends AbstractPropertySerializationTest {

    /**
     * Tests if the {@link SerializableEnumProperty} is serialized correctly.
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

        final SerializableEnumProperty<ModeFX> serializableEnumProperty = new SerializableEnumProperty<>("Test", ModeFX.DrawEllipse);

        oos.writeObject(serializableEnumProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        @SuppressWarnings("unchecked") // If something goes wrong, fail the test
        final SerializableEnumProperty<ModeFX> deserialized = (SerializableEnumProperty<ModeFX>) ois.readObject();

        Assert.assertTrue(getMessage(serializableEnumProperty, deserialized), serializableEnumProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

    /**
     * Tests if {@link SerializableEnumProperty#values()} method works
     * correctly.
     */
    @Test
    public void testValues() {
        final SerializableEnumProperty<ModeFX> serializableBooleanProperty = new SerializableEnumProperty<>("Test", ModeFX.DrawEllipse);

        Assert.assertArrayEquals(serializableBooleanProperty.values(), ModeFX.values());
    }

}
