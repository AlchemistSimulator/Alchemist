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

import it.unibo.alchemist.boundary.gui.view.properties.RangedIntegerProperty;
import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class RangedIntegerPropertySerializationTest extends AbstractPropertySerializationTest {

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final RangedIntegerProperty rangedIntegerProperty = new RangedIntegerProperty("Pippo", 5, 0, 100);

        oos.writeObject(rangedIntegerProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        final RangedIntegerProperty deserialized = (RangedIntegerProperty) ois.readObject();

        Assert.assertTrue(getMessage(rangedIntegerProperty, deserialized), rangedIntegerProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = folder.newFile();

        final RangedIntegerProperty rangedIntegerProperty = new RangedIntegerProperty("Pippo", 5, 0, 100);

        final Writer writer = new FileWriter(file);
        GSON.toJson(rangedIntegerProperty, this.getGsonType(), writer);
        writer.close();

        final Reader reader = new FileReader(file);
        final RangedIntegerProperty deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();

        Assert.assertTrue(rangedIntegerProperty.equals(deserialized));
    }

    @Override
    protected Type getGsonType() {
        return new TypeToken<RangedIntegerProperty>() { }.getType();
    }

}
