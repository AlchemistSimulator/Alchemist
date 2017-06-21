package it.unibo.alchemist.boundary.gui.view.property;

import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.beans.property.Property;

/**
 * Abstract class that provides a common base of methods for properties
 * serialization test.
 */
public abstract class AbstractPropertySerializationTest {
    /**
     * Temporary folder created before each test method, and deleted after each.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /** The {@link Gson} object used for serialization. */
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();

    /**
     * Tests (de)serialization with default Java serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public abstract void testJavaSerialization() throws Exception;

    /**
     * Tests (de)serialization with Google Gson serialization engine.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public abstract void testGsonSerialization() throws Exception;

    /**
     * Method that generate {@link Assert#assertTrue(boolean) assertTrue()}
     * messages.
     * 
     * @param <T>
     *            the class wrapped by this property
     * 
     * @param origin
     *            the original {@link Property}
     * @param deserialized
     *            the deserialized {@link Property}
     * @return the message of test fail
     */
    protected <T> String getMessage(final Property<T> origin, final Property<T> deserialized) {
        return "Property \"" + origin.getName() + ": " + origin.getValue() + "\" is different from property \"" + deserialized.getName()
                + ": " + deserialized.getValue() + "\"";
    }

    /**
     * Returns the {@link Gson} {@link Type}.
     * 
     * @return the Gson type for the tested class
     */
    protected abstract Type getGsonType();

}
