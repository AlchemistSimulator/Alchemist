package it.unibo.alchemist.boundary.gui.view.properties

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import org.eclipse.xtend.lib.annotations.Accessors
import org.junit.experimental.theories.suppliers.TestedOn

/** 
 * This {@link DoubleProperty} is designed to have a range for the wrapped value
 * and to be serializable.
 */
@Accessors(PUBLIC_GETTER, PUBLIC_SETTER)
class RangedDoubleProperty extends DoublePropertyBase implements Serializable {
    /** Default max value of this class. It's the {@link Double#MAX_VALUE max value} of {@link Double} class. */
    static val DEFAULT_MAX_VALUE = Double.MAX_VALUE

    /** Default min value of this class. It's the negative of the {@link Double#MAX_VALUE max value} of {@link Double} class. */
    static val DEFAULT_MIN_VALUE = -Double.MAX_VALUE

    /** Error for exceeding upper bound. */
    protected static final String TOO_BIG_MESSAGE = "Provided value is bigger than the upper bound"
    /** Error for exceeding lower bound. */
    protected static final String TOO_SMALL_MESSAGE = "Provided value is smaller than the lower bound"

    String name
    double lowerBound
    double upperBound

    /**
     * Based on constructor of {@link DoublePropertyBase}, adds the specified
     * bounds.
     * @param name the name of this property
     * @param initialValue the initial value of the wrapped value
     * @param lowerBound the lower bound for the wrapped value to be considered
     * acceptable
     * @param upperBound the upper bound for the wrapped value to be considered
     * acceptable
     */
    new(String name, double initialValue, double lowerBound, double upperBound) {
        this(initialValue, lowerBound, upperBound)
        this.name = name
    }

    /** 
     * Based on constructor of {@link DoublePropertyBase}, adds the specified
     * bounds.
     * @param initialValue the initial value of the wrapped value
     * @param lowerBound the lower bound for the wrapped value to be considered
     * acceptable
     * @param upperBound the upper bound for the wrapped value to be considered
     * acceptable
     */
    new(double initialValue, double lowerBound, double upperBound) {
        super(initialValue)
        this.lowerBound = lowerBound
        this.upperBound = upperBound
    }

    /** 
     * Based on constructor of {@link DoublePropertyBase}, adds the specified
     * bounds.
     * <p>
     * Initial value is set to 0.
     * @param name the name of this property
     * @param lowerBound the lower bound for the wrapped value to be considered
     * acceptable
     * @param upperBound the upper bound for the wrapped value to be considered
     * acceptable
     */
    new(String name, double lowerBound, double upperBound) {
        this(lowerBound, upperBound)
        this.name = name
    }

    /** 
     * Based on constructor of {@link DoublePropertyBase}, adds the specified
     * bounds.
     * <p>
     * Initial value is set to 0.
     * @param lowerBound the lower bound for the wrapped value to be considered
     * acceptable
     * @param upperBound the upper bound for the wrapped value to be considered
     * acceptable
     */
    new(double lowerBound, double upperBound) {
        this(0, lowerBound, upperBound)
    }

    /** 
     * The constructor of {@link DoublePropertyBase}.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     * @param name the name of this property
     * @param initialValue the initial value of the wrapped value
     */
    new(String name, double initialValue) {
        this(initialValue)
        this.name = name
    }

    /** 
     * The constructor of {@link DoublePropertyBase}.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     * @param initialValue the initial value of the wrapped value
     */
    new(double initialValue) {
        this(initialValue, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE)
    }

    /** 
     * The constructor of {@link DoublePropertyBase}.
     * <p>
     * Initial value is set to 0.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     * @param name the name of this property
     */
    new(String name) {
        this()
        this.name = name
    }

    /** 
     * The constructor of {@link DoublePropertyBase}.
     * <p>
     * Initial value is set to 0.
     * <p>
     * Bounds are set to {@link Double#MAX_VALUE} and -{@link Double#MAX_VALUE}.
     */
    new() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE)
    }

    /** 
     * {@inheritDoc}
     * @throws IllegalArgumentException if the provided value is out of the specified range
     */
    override void set(double value) {
        if (value < lowerBound) {
            throw new IllegalArgumentException(TOO_SMALL_MESSAGE)
        }
        if (value > upperBound) {
            throw new IllegalArgumentException(TOO_BIG_MESSAGE)
        }
        super.set(value)
    }

    /** 
     * {@inheritDoc}
     * @throws IllegalArgumentException if the provided value is out of the specified range
     */
    override void setValue(Number value) {
        if (value === null) {
            throw new IllegalArgumentException("Can't set null value")
        }
        if (value.doubleValue() < lowerBound) {
            throw new IllegalArgumentException(TOO_SMALL_MESSAGE)
        }
        if (value.doubleValue() > upperBound) {
            throw new IllegalArgumentException(TOO_BIG_MESSAGE)
        }
        super.setValue(value)
    }

    override Object getBean() { null }

    override String getName() { this.name }

    def private writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(this.getName)
        out.writeDouble(this.getLowerBound)
        out.writeDouble(this.getUpperBound)
        out.writeDouble(this.getValue)
    }

    def private readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.setName(in.readUTF)
        this.setLowerBound(in.readDouble)
        this.setUpperBound(in.readDouble)
        this.setValue(in.readDouble)
    }

    override int hashCode() {
        val prime = 31
        var result = 1

        result = prime * result + this.getLowerBound.hashCode
        result = prime * result + this.getUpperBound.hashCode
        result = prime * result + this.getValue.hashCode
        result = prime * result + if(getName === null) 0 else getName.hashCode
        return result;
    }

    override boolean equals(Object obj) {
        if(this === obj) return true
        if(obj === null) return false
        if(getClass() !== obj.getClass()) return false

        val other = obj as RangedDoubleProperty

        if(this.getLowerBound != other.getLowerBound) return false
        if(this.getUpperBound != other.getUpperBound) return false
        if(this.getValue != other.getValue) return false
        if(this.getName === null && other.getName !== null) return false
        if(this.getName != other.getName) return false

        return true
    }

}
