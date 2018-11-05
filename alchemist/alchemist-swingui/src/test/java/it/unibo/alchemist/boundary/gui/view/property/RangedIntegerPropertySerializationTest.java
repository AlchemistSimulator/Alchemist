package it.unibo.alchemist.boundary.gui.view.property;

import com.google.gson.reflect.TypeToken;
import it.unibo.alchemist.boundary.gui.effects.json.AbstractPropertySerializationTest;
import it.unibo.alchemist.boundary.gui.view.properties.RangedIntegerProperty;
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
import javafx.beans.property.Property;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUint test for custom {@link Property} serialization.
 */
public class RangedIntegerPropertySerializationTest extends AbstractPropertySerializationTest {
    private static final String TEST_NAME = "Pippo";
    private static final int TEST_INITIAL_VALUE = 5;
    private static final int TEST_LOWER_BOUND = 0;
    private static final int TEST_UPPER_BOUND = 100;

    @Test
    @Override
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        final File file = folder.newFile();

        final FileOutputStream fout = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fout);

        final RangedIntegerProperty rangedIntegerProperty = new RangedIntegerProperty(TEST_NAME, TEST_INITIAL_VALUE, TEST_LOWER_BOUND, TEST_UPPER_BOUND);

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

        final RangedIntegerProperty rangedIntegerProperty = new RangedIntegerProperty(TEST_NAME, TEST_INITIAL_VALUE, TEST_LOWER_BOUND, TEST_UPPER_BOUND);

        final Writer writer = new FileWriter(file);
        GSON.toJson(rangedIntegerProperty, this.getGsonType(), writer);
        writer.close();

        final Reader reader = new FileReader(file);
        final RangedIntegerProperty deserialized = GSON.fromJson(reader, this.getGsonType());
        reader.close();

        Assert.assertTrue(getMessage(rangedIntegerProperty, deserialized), rangedIntegerProperty.equals(deserialized));
    }

    @Override
    protected Type getGsonType() {
        return new TypeToken<RangedIntegerProperty>() { }.getType();
    }

    @Override
    protected <T> String getMessage(final Property<T> origin, final Property<T> deserialized) {
        if (origin == null || deserialized == null) {
            return super.getMessage(origin, deserialized);
        }

        return super.getMessage(origin, deserialized)
                + System.lineSeparator() + "Origin range: (" + ((RangedIntegerProperty) origin).getLowerBound()
                + ", " + ((RangedIntegerProperty) origin).getUpperBound() + ")"
                + System.lineSeparator() + "Deserialized range: (" + ((RangedIntegerProperty) deserialized).getLowerBound()
                + ", " + ((RangedIntegerProperty) deserialized).getUpperBound() + ")";
    }
}
