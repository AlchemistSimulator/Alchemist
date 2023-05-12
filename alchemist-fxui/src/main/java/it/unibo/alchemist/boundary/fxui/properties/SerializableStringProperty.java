/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.properties;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import it.unibo.alchemist.boundary.fxui.PropertyTypeAdapter;
import javafx.beans.property.StringPropertyBase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * {@link javafx.beans.property.SimpleStringProperty} that implements also {@link Serializable}.
 */
public class SerializableStringProperty extends StringPropertyBase implements Serializable {
    /**
     * Default Serial Version UID.
     */
    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * The constructor of {@code SimpleStringProperty}.
     */
    public SerializableStringProperty() {
        this("", "");
    }

    /**
     * The constructor of {@code SimpleStringProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public SerializableStringProperty(final String initialValue) {
        this("", initialValue);
    }

    /**
     * The constructor of {@code SimpleStringProperty}.
     *
     * @param name         the name of this {@code SimpleStringProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public SerializableStringProperty(final String name, final String initialValue) {
        super();
        this.name = name;
        this.setValue(initialValue);
    }

    /**
     * Returns a {@link com.google.gson.JsonSerializer} and {@link com.google.gson.JsonDeserializer} combo class
     * to be used as a {@code TypeAdapter} for this
     * {@code SerializableStringProperty}.
     *
     * @return the {@code TypeAdapter} for this class
     */
    public static PropertyTypeAdapter<SerializableStringProperty> getTypeAdapter() {
        return new PropertyTypeAdapter<>() {

            @Override
            public SerializableStringProperty deserialize(
                final JsonElement json,
                final Type typeOfT,
                final JsonDeserializationContext context
            ) {
                final JsonObject jObj = json.getAsJsonObject();
                final String name = jObj.get(NAME).getAsString();
                final String value = jObj.get(VALUE).getAsString();
                return new SerializableStringProperty(name, value);
            }

            @Override
            public JsonElement serialize(
                final SerializableStringProperty src,
                final Type typeOfSrc,
                final JsonSerializationContext context
            ) {
                final JsonObject jObj = new JsonObject();
                final String name = src.getName();
                jObj.addProperty(NAME, name);
                final String value = src.getValue();
                jObj.addProperty(VALUE, value);
                return jObj;
            }

        };
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
     * @param name the name to give to the property
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
     * @param out the output stream
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
     * @param in the input stream
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.setName(in.readUTF());
        this.setValue(in.readUTF());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getName());
    }

    /**
     * {@inheritDoc}
     */
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
            return other.getName() == null;
        } else {
            return getName().equals(other.getName());
        }
    }

}
