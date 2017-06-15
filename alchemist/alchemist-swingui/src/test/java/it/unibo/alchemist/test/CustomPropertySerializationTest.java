package it.unibo.alchemist.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import it.unibo.alchemist.boundary.gui.view.property.RangedDoubleProperty;

public class CustomPropertySerializationTest {
    /**
     * Temporary folder created before each test method, and deleted after each.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Tests if the {@link RangedDoubleProperty} is serialized correctly.
     * 
     * @throws IOException
     *             if I/O problems occur
     * @throws ClassNotFoundException
     *             if a problem occurs during deserialization
     */
    @Test
    public void testRangedDoubleProperty() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final RangedDoubleProperty rangedDoubleProperty = new RangedDoubleProperty(null, "Pippo", 5.0, 0.0, 100.0);

        oos.writeObject(rangedDoubleProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final RangedDoubleProperty deserialized = (RangedDoubleProperty) ois.readObject();

        Assert.assertTrue(rangedDoubleProperty.getName() + ": " + rangedDoubleProperty.get() + " is different from "
                + deserialized.getName() + ": " + deserialized.get(), rangedDoubleProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

}
