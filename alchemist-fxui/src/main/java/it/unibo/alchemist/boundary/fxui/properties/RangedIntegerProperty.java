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
import javafx.beans.property.IntegerPropertyBase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * This {@link javafx.beans.property.IntegerProperty} is designed to have a range for the wrapped
 * value and to be serializable.
 */
public class RangedIntegerProperty extends IntegerPropertyBase implements Serializable {
    /** Default Serial Version UID. */
    private static final long serialVersionUID = 1L;

    private static final Integer DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
    private static final Integer DEFAULT_MIN_VALUE = Integer.MIN_VALUE;
    /** Error for exceeding upper bound. */
    protected static final String TOO_BIG_MESSAGE = "Provided value is bigger than the upper bound";
    /** Error for exceeding lower bound. */
    protected static final String TOO_SMALL_MESSAGE = "Provided value is smaller than the lower bound";

    private int lowerBound;
    private int upperBound;

    private String name;

    /**
     * Based on constructor of {@link IntegerPropertyBase}, adds the specified
     * bounds.
     *
     * @param name
     *            the name of this {@code IntegerProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedIntegerProperty(final String name, final int initialValue, final int lowerBound, final int upperBound) {
        super(initialValue);
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Based on constructor of {@link IntegerPropertyBase}, adds the specified
     * bounds.
     * <p>
     * Initial value is set to 0.
     *
     * @param name
     *            the name of this {@code IntegerProperty}
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedIntegerProperty(final String name, final int lowerBound, final int upperBound) {
        this(name, 0, lowerBound, upperBound);
    }

    /**
     * Based on constructor of {@link IntegerPropertyBase}, adds the specified
     * bounds.
     *
     * @param initialValue
     *            the initial value of the wrapped value
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedIntegerProperty(final int initialValue, final int lowerBound, final int upperBound) {
        this("", initialValue, lowerBound, upperBound);
    }

    /**
     * Based on constructor of {@link IntegerPropertyBase}, adds the specified
     * bounds.
     * <p>
     * Initial value is set to 0.
     * 
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedIntegerProperty(final int lowerBound, final int upperBound) {
        this(0, lowerBound, upperBound);
    }

    /**
     * The constructor of {@link IntegerPropertyBase}.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * {@link Integer#MIN_VALUE}.
     *
     * @param name
     *            the name of this {@code IntegerProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public RangedIntegerProperty(final String name, final int initialValue) {
        this(name, initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link IntegerPropertyBase}.
     * <p>
     * Initial value is set to 0.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * {@link Integer#MIN_VALUE}.
     *
     * @param name
     *            the name of this {@code IntegerProperty}
     */
    public RangedIntegerProperty(final String name) {
        this(name, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link IntegerPropertyBase}.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * {@link Integer#MIN_VALUE}.
     *
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public RangedIntegerProperty(final int initialValue) {
        this(initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link IntegerPropertyBase}.
     * <p>
     * Initial value is set to 0.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * {@link Integer#MIN_VALUE}.
     */
    public RangedIntegerProperty() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
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
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if the provided value is out of the specified range
     */
    @Override
    public void set(final int value) {
        if (value < lowerBound) {
            throw new IllegalArgumentException(TOO_SMALL_MESSAGE);
        }
        if (value > upperBound) {
            throw new IllegalArgumentException(TOO_BIG_MESSAGE);
        }
        super.set(value);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if the provided value is out of the specified range
     */
    @Override
    public void setValue(final Number value) {
        if (value == null) {
            throw new IllegalArgumentException("Can't set null value");
        } else {
            if (value.intValue() < lowerBound) {
                throw new IllegalArgumentException(TOO_SMALL_MESSAGE);
            }
            if (value.intValue() > upperBound) {
                throw new IllegalArgumentException(TOO_BIG_MESSAGE);
            }
            super.setValue(value);
        }
    }

    /**
     * Getter method for the lower bound.
     * 
     * @return the lower bound
     */
    public int getLowerBound() {
        return lowerBound;
    }

    /**
     * Setter method for the lower bound.
     * 
     * @param lowerBound
     *            the lower bound
     */
    public void setLowerBound(final int lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * Getter method for the upper bound.
     * 
     * @return the upper bound
     */
    public int getUpperBound() {
        return upperBound;
    }

    /**
     * Setter method for the upper bound.
     * 
     * @param upperBound
     *            the upper bound
     */
    public void setUpperBound(final int upperBound) {
        this.upperBound = upperBound;
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
        out.writeInt(this.getLowerBound());
        out.writeInt(this.getUpperBound());
        out.writeInt(this.getValue());
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
        this.setLowerBound(in.readInt());
        this.setUpperBound(in.readInt());
        this.setValue(in.readInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getLowerBound(), getUpperBound(), getValue(), getName());
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
        final RangedIntegerProperty other = (RangedIntegerProperty) obj;
        if (getLowerBound() != other.getLowerBound()) {
            return false;
        }
        if (getUpperBound() != other.getUpperBound()) {
            return false;
        }
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

    /**
     * Returns a {@link com.google.gson.JsonSerializer} and {@link com.google.gson.JsonDeserializer} combo class
     * to be used as a {@code TypeAdapter} for this
     * {@code RangedIntegerProperty}.
     * 
     * @return the {@code TypeAdapter} for this class
     */
    public static PropertyTypeAdapter<RangedIntegerProperty> getTypeAdapter() {
        return new PropertyTypeAdapter<>() {
            private static final String LOWER_BOUND = "lower bound";
            private static final String UPPER_BOUND = "upper bound";

            @Override
            public RangedIntegerProperty deserialize(
                final JsonElement json,
                final Type typeOfT,
                final JsonDeserializationContext context
            ) {
                final JsonObject jObj = json.getAsJsonObject();
                final String name = jObj.get(NAME).getAsString();
                final int value = jObj.get(VALUE).getAsInt();
                final int lowerBound = jObj.get(LOWER_BOUND).getAsInt();
                final int upperBound = jObj.get(UPPER_BOUND).getAsInt();
                return new RangedIntegerProperty(name, value, lowerBound, upperBound);
            }

            @Override
            public JsonElement serialize(
                final RangedIntegerProperty src,
                final Type typeOfSrc,
                final JsonSerializationContext context
            ) {
                final JsonObject jObj = new JsonObject();
                final String name = src.getName();
                jObj.addProperty(NAME, name);
                final int value = src.getValue();
                jObj.addProperty(VALUE, value);
                final int lowerBound = src.getLowerBound();
                jObj.addProperty(LOWER_BOUND, lowerBound);
                final int upperBound = src.getUpperBound();
                jObj.addProperty(UPPER_BOUND, upperBound);
                return jObj;
            }

        };
    }
}
