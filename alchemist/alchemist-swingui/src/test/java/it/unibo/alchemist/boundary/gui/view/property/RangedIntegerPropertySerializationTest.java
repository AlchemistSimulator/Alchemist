package it.unibo.alchemist.boundary.gui.view.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import it.unibo.alchemist.boundary.gui.view.properties.RangedIntegerProperty;
import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class RangedIntegerPropertySerializationTest extends AbstractPropertySerializationTest {

    /**
     * Tests if the {@link RangedIntegerProperty} is serialized correctly.
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

        final RangedIntegerProperty rangedDoubleProperty = new RangedIntegerProperty("Pippo", 5, 0, 100);

        oos.writeObject(rangedDoubleProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final RangedIntegerProperty deserialized = (RangedIntegerProperty) ois.readObject();

        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

}
