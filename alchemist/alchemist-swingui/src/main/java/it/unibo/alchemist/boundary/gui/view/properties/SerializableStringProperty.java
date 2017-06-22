package it.unibo.alchemist.boundary.gui.view.properties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

/**
 * {@link SimpleStringProperty} that implements also {@link Serializable}.
 */
public class SerializableStringProperty extends StringPropertyBase implements Serializable {
    /** Generated Serial Version UID. */
    private static final long serialVersionUID = -3684192876864701055L;

    private String name;

    /**
     * The constructor of {@code SimpleStringProperty}.
     */
    public SerializableStringProperty() {
        super();
        this.name = "";
    }

    /**
     * The constructor of {@code SimpleStringProperty}.
     *
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public SerializableStringProperty(final String initialValue) {
        this();
        this.setValue(initialValue);
    }

    /**
     * The constructor of {@code SimpleStringProperty}.
     *
     * @param name
     *            the name of this {@code SimpleStringProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public SerializableStringProperty(final String name, final String initialValue) {
        this();
        this.name = name;
        this.setValue(initialValue);
    }

    /**
     * Getter method for unused field bean.
     * 
     * @return null
     */
    @Override
    public Object getBean() {
        return null;
    }

    /**
     * Getter method for the name.
     * 
     * @return the name to give to the property
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter method for the name.
     * 
     * @param name
     *            the name to give to the property
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Method needed for well working serialization.
     * <p>
     * From {@link Serializable}: <blockquote>The {@code writeObject} method is
     * responsible for writing the state of the object for its particular class
     * so that the corresponding readObject method can restore it. The default
     * mechanism for saving the Object's fields can be invoked by calling
     * {@code out.defaultWriteObject}. The method does not need to concern
     * itself with the state belonging to its superclasses or subclasses. State
     * is saved by writing the 3 individual fields to the
     * {@code ObjectOutputStream} using the {@code writeObject} method or by
     * using the methods for primitive data types supported by
     * {@code DataOutput}. </blockquote>
     * 
     * @param out
     *            the output stream
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(this.getName());
        out.writeUTF(this.getValue());
    }

    /**
     * Method needed for well working serialization.
     * <p>
     * From {@link Serializable}: <blockquote>The {@code readObject} method is
     * responsible for reading from the stream and restoring the classes fields.
     * It may call {@code in.defaultReadObject} to invoke the default mechanism
     * for restoring the object's non-static and non-transient fields. The
     * {@code defaultReadObject} method uses information in the stream to assign
     * the fields of the object saved in the stream with the correspondingly
     * named fields in the current object. This handles the case when the class
     * has evolved to add new fields. The method does not need to concern itself
     * with the state belonging to its superclasses or subclasses. State is
     * saved by writing the individual fields to the {@code ObjectOutputStream}
     * using the {@code writeObject} method or by using the methods for
     * primitive data types supported by {@code DataOutput}. </blockquote>
     * 
     * @param in
     *            the input stream
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.setName(in.readUTF());
        this.setValue(in.readUTF());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SerializableStringProperty other = (SerializableStringProperty) obj;
        if (getValue() == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else if (!getValue().equals(other.getValue())) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }

    /**
     * Returns a {@link JsonSerializer} and {@link JsonDeserializer} combo class
     * to be used as a {@code TypeAdapter} for this
     * {@code SerializableStringProperty}.
     * 
     * @return the {@code TypeAdapter} for this class
     */
    public static PropertyTypeAdapter<SerializableStringProperty> getPropertyTypeAdapter() {
        return new PropertyTypeAdapter<SerializableStringProperty>() {

            @Override
            public SerializableStringProperty deserialize(final JsonElement json, final Type typeOfT,
                    final JsonDeserializationContext context) {
                final JsonObject jObj = (JsonObject) json;

                final String name = jObj.get(NAME).getAsString();
                final String value = jObj.get(VALUE).getAsString();

                return new SerializableStringProperty(name, value);
            }

            @Override
            public JsonElement serialize(final SerializableStringProperty src, final Type typeOfSrc,
                    final JsonSerializationContext context) {
                final JsonObject jObj = new JsonObject();

                final String name = src.getName();
                jObj.addProperty(NAME, name);
                final String value = src.getValue();
                jObj.addProperty(VALUE, value);

                return jObj;
            }

        };
    }

}
