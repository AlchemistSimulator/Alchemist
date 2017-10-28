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

import it.unibo.alchemist.boundary.gui.view.properties.PropertyFactory;
import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty;
import javafx.beans.property.Property;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class RangedDoublePropertySerializationTest extends AbstractPropertySerializationTest {

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        // CHECKSTYLE:OFF
        RangedDoubleProperty rangedDoubleProperty = new RangedDoubleProperty("Pippo", 5.0, 0.0, 100.0);
        // CHECKSTYLE:ON
        oos.writeObject(rangedDoubleProperty);

        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);

        RangedDoubleProperty deserialized = (RangedDoubleProperty) ois.readObject();

        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));

        // CHECKSTYLE:OFF
        rangedDoubleProperty = PropertyFactory.getColorChannelProperty("RED", 250.0);
        // CHECKSTYLE:ON
        oos.writeObject(rangedDoubleProperty);

        deserialized = (RangedDoubleProperty) ois.readObject();

        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));

        // CHECKSTYLE:OFF
        rangedDoubleProperty = PropertyFactory.getPercentageRangedProperty("Percent test", 33.0);
        // CHECKSTYLE:ON
        oos.writeObject(rangedDoubleProperty);

        deserialized = (RangedDoubleProperty) ois.readObject();

        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));

        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = folder.newFile();

        // CHECKSTYLE:OFF
        RangedDoubleProperty rangedDoubleProperty = new RangedDoubleProperty("Pippo", 5.0, 0.0, 100.0);
        // CHECKSTYLE:ON

        final Writer writer = new FileWriter(file);
        GSON.toJson(rangedDoubleProperty, this.getGsonType(), writer);
        writer.flush();
        final Reader reader = new FileReader(file);
        RangedDoubleProperty deserialized = GSON.fromJson(reader, this.getGsonType());

        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));

        // CHECKSTYLE:OFF
        rangedDoubleProperty = PropertyFactory.getColorChannelProperty("RED", 250.0);
        // CHECKSTYLE:ON
        GSON.toJson(rangedDoubleProperty, this.getGsonType(), writer);
        writer.flush();
        deserialized = GSON.fromJson(reader, this.getGsonType());

        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));

        // CHECKSTYLE:OFF
        rangedDoubleProperty = PropertyFactory.getPercentageRangedProperty("Percent test", 33.0);
        // CHECKSTYLE:ON
        GSON.toJson(rangedDoubleProperty, this.getGsonType(), writer);
        writer.close();
        deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();
        Assert.assertTrue(getMessage(rangedDoubleProperty, deserialized), rangedDoubleProperty.equals(deserialized));
    }

    @Override
    protected Type getGsonType() {
        return new TypeToken<RangedDoubleProperty>() { }.getType();
    }

    @Override
    protected <T> String getMessage(final Property<T> origin, final Property<T> deserialized) {
        if (origin == null || deserialized == null) {
            return super.getMessage(origin, deserialized);
        }

        return super.getMessage(origin, deserialized)
                + System.lineSeparator() + "Origin range: (" + ((RangedDoubleProperty) origin).getLowerBound()
                + ", " + ((RangedDoubleProperty) origin).getUpperBound() + ")"
                + System.lineSeparator() + "Deserialized range: (" + ((RangedDoubleProperty) deserialized).getLowerBound()
                + ", " + ((RangedDoubleProperty) deserialized).getUpperBound() + ")";
    }

}
