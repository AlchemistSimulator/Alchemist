package it.unibo.alchemist.boundary.gui.view.property;

import java.io.Serializable;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * This {@link DoubleProperty} is designed to have a range for the wrapped value
 * and to be serializable.
 */
public class RangedDoublePropertyOld extends SimpleDoubleProperty implements /*Externalizable*/ Serializable {
    /** Generated Serial Version UID. */
    private static final long serialVersionUID = -8459149578353859712L;
    private static final Double DEFAULT_MAX_VALUE = Double.MAX_VALUE;
    private static final Double DEFAULT_MIN_VALUE = -Double.MAX_VALUE;
    /** Error for exceeding upper bound. */
    protected static final String TOO_BIG_MESSAGE = "Provided value is bigger than the upper bound";
    /** Error for exceeding lower bound. */
    protected static final String TOO_SMALL_MESSAGE = "Provided value is smaller than the lower bound";

    private Double lowerBound;
    private Double upperBound;

    /**
     * Based on constructor of {@link SimpleDoubleProperty}, adds the specified
     * bounds.
     *
     * @param bean
     *            the bean of this {@code DoubleProperty}
     * @param name
     *            the name of this {@code DoubleProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedDoublePropertyOld(final Object bean, final String name, final double initialValue, final Double lowerBound,
            final Double upperBound) {
        super(bean, name, initialValue);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Based on constructor of {@link SimpleDoubleProperty}, adds the specified
     * bounds.
     * <p>
     * Initial value is set to 0.
     *
     * @param bean
     *            the bean of this {@code DoubleProperty}
     * @param name
     *            the name of this {@code DoubleProperty}
     * @param lowerBound
     *            the lower bound for the wrapped value to be considered
     *            acceptable
     * @param upperBound
     *            the upper bound for the wrapped value to be considered
     *            acceptable
     */
    public RangedDoublePropertyOld(final Object bean, final String name, final Double lowerBound, final Double upperBound) {
        this(bean, name, 0, lowerBound, upperBound);
    }

    /**
     * Based on constructor of {@link SimpleDoubleProperty}, adds the specified
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
    public RangedDoublePropertyOld(final double initialValue, final Double lowerBound, final Double upperBound) {
        super(initialValue);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Based on constructor of {@link SimpleDoubleProperty}, adds the specified
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
    public RangedDoublePropertyOld(final Double lowerBound, final Double upperBound) {
        this((upperBound - lowerBound) / 2, lowerBound, upperBound);
    }

    /**
     * The constructor of {@link SimpleDoubleProperty}.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     *
     * @param bean
     *            the bean of this {@code DoubleProperty}
     * @param name
     *            the name of this {@code DoubleProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public RangedDoublePropertyOld(final Object bean, final String name, final double initialValue) {
        this(bean, name, initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link SimpleDoubleProperty}.
     * <p>
     * Initial value is set to 0.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     *
     * @param bean
     *            the bean of this {@code DoubleProperty}
     * @param name
     *            the name of this {@code DoubleProperty}
     */
    public RangedDoublePropertyOld(final Object bean, final String name) {
        this(bean, name, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link SimpleDoubleProperty}.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     *
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public RangedDoublePropertyOld(final double initialValue) {
        this(initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * The constructor of {@link SimpleDoubleProperty}.
     * <p>
     * Initial value is set to 0.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     */
    public RangedDoublePropertyOld() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if the provided value is out of the specified range
     */
    @Override
    public void set(final double value) {
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
            if (value.doubleValue() < lowerBound) {
                throw new IllegalArgumentException(TOO_SMALL_MESSAGE);
            }
            if (value.doubleValue() > upperBound) {
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
    public Double getLowerBound() {
        return lowerBound;
    }

    /**
     * Setter method for the lower bound.
     * 
     * @param lowerBound
     *            the lower bound
     */
    public void setLowerBound(final Double lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * Getter method for the upper bound.
     * 
     * @return the upper bound
     */
    public Double getUpperBound() {
        return upperBound;
    }

    /**
     * Setter method for the upper bound.
     * 
     * @param upperBound
     *            the upper bound
     */
    public void setUpperBound(final Double upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getBean() == null) ? 0 : getBean().hashCode());
        result = prime * result + ((getLowerBound() == null) ? 0 : getLowerBound().hashCode());
        result = prime * result + ((getUpperBound() == null) ? 0 : getUpperBound().hashCode());
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
        final RangedDoublePropertyOld other = (RangedDoublePropertyOld) obj;
        if (getBean() == null) {
            if (other.getBean() != null) {
                return false;
            }
        } else if (!getBean().equals(other.getBean())) {
            return false;
        }
        if (getLowerBound() == null) {
            if (other.getLowerBound() != null) {
                return false;
            }
        } else if (!getLowerBound().equals(other.getLowerBound())) {
            return false;
        }
        if (getUpperBound() == null) {
            if (other.getUpperBound() != null) {
                return false;
            }
        } else if (!getUpperBound().equals(other.getUpperBound())) {
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

//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(this.getBean());
//        out.writeDouble(this.getLowerBound());
//        out.writeDouble(this.getUpperBound());
//        out.writeDouble(this.getValue());
//        out.writeChars(this.getName());
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//
//    }
}
