package it.unibo.alchemist.boundary.gui.view.properties;

import java.io.Serializable;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * This {@link IntegerProperty} is designed to have a range for the wrapped
 * value and to be serializable.
 */
public class RangedIntegerProperty extends SimpleIntegerProperty implements Serializable {
    /** Generated Serial Version UID. */
    private static final long serialVersionUID = -897269650389301324L;

    private static final Integer DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
    private static final Integer DEFAULT_MIN_VALUE = Integer.MIN_VALUE;
    /** Error for exceeding upper bound. */
    protected static final String TOO_BIG_MESSAGE = "Provided value is bigger than the upper bound";
    /** Error for exceeding lower bound. */
    protected static final String TOO_SMALL_MESSAGE = "Provided value is smaller than the lower bound";

    private Integer lowerBound;
    private Integer upperBound;

    /**
     * Based on constructor of {@link SimpleIntegerProperty}, adds the specified
     * bounds.
     *
     * @param bean
     *            the bean of this {@code IntegerProperty}
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
    public RangedIntegerProperty(final Object bean, final String name, final int initialValue, final Integer lowerBound,
            final Integer upperBound) {
        super(bean, name, initialValue);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Based on constructor of {@link SimpleIntegerProperty}, adds the specified
     * bounds.
     * <p>
     * Initial value is set as the half of the specified range.
     *
     * @param bean
     *            the bean of this {@code IntegerProperty}
     * @param name
     *            the name of this {@code IntegerProperty}
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedIntegerProperty(final Object bean, final String name, final Integer lowerBound, final Integer upperBound) {
        this(bean, name, (upperBound - lowerBound) / 2, lowerBound, upperBound);
    }

    /**
     * Based on constructor of {@link SimpleIntegerProperty}, adds the specified
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
    public RangedIntegerProperty(final int initialValue, final Integer lowerBound, final Integer upperBound) {
        super(initialValue);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Based on constructor of {@link SimpleIntegerProperty}, adds the specified
     * bounds.
     * <p>
     * Initial value is set as the half of the specified range.
     * 
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedIntegerProperty(final Integer lowerBound, final Integer upperBound) {
        this((upperBound - lowerBound) / 2, lowerBound, upperBound);
    }

    /**
     * The constructor of {@link SimpleIntegerProperty}.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * {@link Integer#MIN_VALUE}.
     *
     * @param bean
     *            the bean of this {@code IntegerProperty}
     * @param name
     *            the name of this {@code IntegerProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public RangedIntegerProperty(final Object bean, final String name, final int initialValue) {
        this(bean, name, initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link SimpleIntegerProperty}.
     * <p>
     * Initial value is set as the half of the specified range.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * {@link Integer#MIN_VALUE}.
     *
     * @param bean
     *            the bean of this {@code IntegerProperty}
     * @param name
     *            the name of this {@code IntegerProperty}
     */
    public RangedIntegerProperty(final Object bean, final String name) {
        this(bean, name, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link SimpleIntegerProperty}.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * -{@link Integer#MIN_VALUE}.
     *
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public RangedIntegerProperty(final int initialValue) {
        this(initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link SimpleIntegerProperty}.
     * <p>
     * Initial value is set as the half of the specified range.
     * <p>
     * Bounds are set to {@link Integer#MAX_VALUE} and
     * -{@link Integer#MIN_VALUE}.
     */
    public RangedIntegerProperty() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
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
    public Integer getLowerBound() {
        return lowerBound;
    }

    /**
     * Setter method for the lower bound.
     * 
     * @param lowerBound
     *            the lower bound
     */
    public void setLowerBound(final Integer lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * Getter method for the upper bound.
     * 
     * @return the upper bound
     */
    public Integer getUpperBound() {
        return upperBound;
    }

    /**
     * Setter method for the upper bound.
     * 
     * @param upperBound
     *            the upper bound
     */
    public void setUpperBound(final Integer upperBound) {
        this.upperBound = upperBound;
    }

    // @Override
    // public int hashCode() {
    // final int prime = 31;
    // int result = 1;
    // result = prime * result + ((getBean() == null) ? 0 :
    // getBean().hashCode());
    // result = prime * result + ((getLowerBound() == null) ? 0 :
    // getLowerBound().hashCode());
    // result = prime * result + ((getUpperBound() == null) ? 0 :
    // getUpperBound().hashCode());
    // result = prime * result + ((getValue() == null) ? 0 :
    // getValue().hashCode());
    // result = prime * result + ((getName() == null) ? 0 :
    // getName().hashCode());
    // return result;
    // }
    //
    // @Override
    // public boolean equals(final Object obj) {
    // if (this == obj) {
    // return true;
    // }
    // if (obj == null) {
    // return false;
    // }
    // if (getClass() != obj.getClass()) {
    // return false;
    // }
    // final RangedIntegerProperty other = (RangedIntegerProperty) obj;
    // if (getBean() == null) {
    // if (other.getBean() != null) {
    // return false;
    // }
    // } else if (!getBean().equals(other.getBean())) {
    // return false;
    // }
    // if (getLowerBound() == null) {
    // if (other.getLowerBound() != null) {
    // return false;
    // }
    // } else if (!getLowerBound().equals(other.getLowerBound())) {
    // return false;
    // }
    // if (getUpperBound() == null) {
    // if (other.getUpperBound() != null) {
    // return false;
    // }
    // } else if (!getUpperBound().equals(other.getUpperBound())) {
    // return false;
    // }
    // if (getValue() == null) {
    // if (other.getValue() != null) {
    // return false;
    // }
    // } else if (!getValue().equals(other.getValue())) {
    // return false;
    // }
    // if (getName() == null) {
    // if (other.getName() != null) {
    // return false;
    // }
    // } else if (!getName().equals(other.getName())) {
    // return false;
    // }
    // return true;
    // }
}
