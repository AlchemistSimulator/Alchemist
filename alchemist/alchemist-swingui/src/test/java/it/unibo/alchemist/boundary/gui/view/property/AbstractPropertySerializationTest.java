package it.unibo.alchemist.boundary.gui.view.property;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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

    /**
     * Main test.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public abstract void test() throws Exception;

    /**
     * Method that generate {@link Assert#assertTrue(boolean) assertTrue()}
     * messages.
     * 
     * @param <T>
     *            the generic type that the property wraps
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

}
