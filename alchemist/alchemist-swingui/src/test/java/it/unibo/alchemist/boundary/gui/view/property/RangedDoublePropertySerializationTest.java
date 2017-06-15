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
public class RangedDoublePropertySerializationTest extends AbstractPropertySerializationTest {

    /**
     * Tests if the {@link RangedDoublePropertyOld} is serialized correctly.
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

        final RangedDoubleProperty rangedDoubleProperty = new RangedDoubleProperty("Pippo", 5.0, 0.0, 100.0);

        oos.writeObject(rangedDoubleProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final RangedDoubleProperty deserialized = (RangedDoubleProperty) ois.readObject();

        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

}
