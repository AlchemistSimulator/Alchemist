package it.unibo.alchemist.boundary.gui.view.property;

import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractPropertySerializationTest;
import it.unibo.alchemist.boundary.gui.view.properties.PropertyFactory;
import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty;
import it.unibo.alchemist.test.TemporaryFile;
import javafx.beans.property.Property;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test for custom {@link Property} serialization.
 */
public final class RangedDoublePropertySerializationTest extends AbstractPropertySerializationTest {
    private static final String TEST_NAME = "Pippo";
    private static final double TEST_INITIAL_VALUE = 5.0;
    private static final double TEST_LOWER_BOUND = 0.0;
    private static final double TEST_UPPER_BOUND = 100.0;
    private static final String TEST_PERCENT_NAME = "Percent test";
    private static final double TEST_PERCENT_INITIAL_VALUE = 33.0;
    private static final String TEST_COLOR_NAME = "RED";
    private static final double TEST_COLOR_INITIAL_VALUE = 0.5;

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = TemporaryFile.create();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        RangedDoubleProperty rangedDoubleProperty = new RangedDoubleProperty(TEST_NAME, TEST_INITIAL_VALUE, TEST_LOWER_BOUND, TEST_UPPER_BOUND);
        oos.writeObject(rangedDoubleProperty);
        final FileInputStream fin = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fin);
        RangedDoubleProperty deserialized = (RangedDoubleProperty) ois.readObject();
        assertEquals(rangedDoubleProperty, deserialized, getMessage(rangedDoubleProperty, deserialized));
        rangedDoubleProperty = PropertyFactory.getFXColorChannelProperty(TEST_COLOR_NAME, TEST_COLOR_INITIAL_VALUE);
        oos.writeObject(rangedDoubleProperty);
        deserialized = (RangedDoubleProperty) ois.readObject();
        assertEquals(rangedDoubleProperty, deserialized, getMessage(rangedDoubleProperty, deserialized));
        rangedDoubleProperty = PropertyFactory.getPercentageRangedProperty(TEST_PERCENT_NAME, TEST_PERCENT_INITIAL_VALUE);
        oos.writeObject(rangedDoubleProperty);
        deserialized = (RangedDoubleProperty) ois.readObject();
        assertEquals(rangedDoubleProperty, deserialized, getMessage(rangedDoubleProperty, deserialized));
        oos.close();
        ois.close();
    }

    @Test
    @Override
    public void testGsonSerialization() throws Exception {
        final File file = TemporaryFile.create();
        RangedDoubleProperty rangedDoubleProperty = new RangedDoubleProperty(TEST_NAME, TEST_INITIAL_VALUE, TEST_LOWER_BOUND, TEST_UPPER_BOUND);
        final Writer writer = new FileWriter(file);
        GSON.toJson(rangedDoubleProperty, this.getGsonType(), writer);
        writer.flush();
        final Reader reader = new FileReader(file);
        RangedDoubleProperty deserialized = GSON.fromJson(reader, this.getGsonType());
        assertEquals(rangedDoubleProperty, deserialized, getMessage(rangedDoubleProperty, deserialized));
        rangedDoubleProperty = PropertyFactory.getFXColorChannelProperty(TEST_COLOR_NAME, TEST_COLOR_INITIAL_VALUE);
        GSON.toJson(rangedDoubleProperty, this.getGsonType(), writer);
        writer.flush();
        deserialized = GSON.fromJson(reader, this.getGsonType());
        assertEquals(rangedDoubleProperty, deserialized, getMessage(rangedDoubleProperty, deserialized));
        rangedDoubleProperty = PropertyFactory.getPercentageRangedProperty(TEST_PERCENT_NAME, TEST_PERCENT_INITIAL_VALUE);
        GSON.toJson(rangedDoubleProperty, this.getGsonType(), writer);
        writer.close();
        deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();
        assertEquals(rangedDoubleProperty, deserialized, getMessage(rangedDoubleProperty, deserialized));
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
