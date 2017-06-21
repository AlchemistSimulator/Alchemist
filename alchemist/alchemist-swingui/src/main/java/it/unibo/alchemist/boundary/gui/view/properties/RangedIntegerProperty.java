package it.unibo.alchemist.boundary.gui.view.properties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;

/**
 * This {@link IntegerProperty} is designed to have a range for the wrapped
 * value and to be serializable.
 */
public class RangedIntegerProperty extends IntegerPropertyBase implements Serializable {
    /** Generated Serial Version UID. */
    private static final long serialVersionUID = -897269650389301324L;

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
        out.writeInt((this.getValue()).intValue());
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
        this.setValue(Integer.valueOf(in.readInt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Integer.valueOf(getLowerBound()).hashCode();
        result = prime * result + Integer.valueOf(getUpperBound()).hashCode();
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
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }
}
