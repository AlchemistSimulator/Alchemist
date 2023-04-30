/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.properties;

import javafx.beans.property.ObjectPropertyBase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * {@link javafx.beans.property.Property} designed to wrap an {@link Enum enum}.
 * <p>
 * It is based on {@code ObjectPropertyBase} and is {@code Serializable}.
 * 
 * @param <T>
 *            the enumeration wrapped
 */
public class SerializableEnumProperty<T extends Enum<T>> extends ObjectPropertyBase<T> implements Serializable {
    /** Default Serial Version UID. */
    private static final long serialVersionUID = 1L;
    private String name;

    /**
     * The constructor of {@code ObjectPropertyBase}.
     */
    public SerializableEnumProperty() {
        super();
    }

    /**
     * The constructor of {@code ObjectPropertyBase}.
     *
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public SerializableEnumProperty(final T initialValue) {
        super(initialValue);
    }

    /**
     * The constructor of {@code ObjectPropertyBase}.
     *
     * @param name
     *            the name of this {@code SimpleObjectProperty}
     */
    public SerializableEnumProperty(final String name) {
        this();
        this.name = name;
    }

    /**
     * The constructor of {@code ObjectPropertyBase}.
     *
     * @param name
     *            the name of this {@code SimpleObjectProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public SerializableEnumProperty(final String name, final T initialValue) {
        this(initialValue);
        this.name = name;
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
     * Getter method for the name of the property.
     * 
     * @return the name of the property
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter method for the name of the property.
     * 
     * @param name
     *            the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the elements of the enum class wrapped by this {@link javafx.beans.property.Property}.
     * 
     * @return the elements of the enum class
     * @throws IllegalStateException
     *             If this {@link javafx.beans.property.Property} is not wrapping any enum
     */
    public T[] values() {
        final T enumeration = this.getValue();
        if (enumeration != null) {
            return enumeration.getDeclaringClass().getEnumConstants();
        } else {
            throw new IllegalStateException("This Property is not wrapping any enum");
        }
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
        out.writeObject(this.getValue());
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
    @SuppressWarnings("unchecked") // Should be of the right class
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.setName(in.readUTF());
        this.setValue((T) in.readObject());
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
        final SerializableEnumProperty<?> other = (SerializableEnumProperty<?>) obj;
        if (this.getValue() == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else if (!this.getValue().equals(other.getValue())) {
            return false;
        }
        if (this.getName() == null) {
            return other.getName() == null;
        } else {
            return getName().equals(other.getName());
        }
    }
}
