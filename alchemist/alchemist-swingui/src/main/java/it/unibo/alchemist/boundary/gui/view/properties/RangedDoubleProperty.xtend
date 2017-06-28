package it.unibo.alchemist.boundary.gui.view.properties

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import org.eclipse.xtend.lib.annotations.Accessors
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer
import it.unibo.alchemist.boundary.gui.view.properties.PropertyTypeAdapter
import com.google.gson.JsonElement
import java.lang.reflect.Type
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonObject

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
        super(if (initialValue >= lowerBound && initialValue <= upperBound) initialValue 
            else throw new IllegalArgumentException("Value must be between bounds")
        ) 
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

    /**
     * Getter method for unused field bean.
     * 
     * @return null
     */
    override Object getBean() { null }

    /**
     * Getter method for name.
     * 
     * @return the name of the property
     */
    override String getName() { this.name }

    /**
     * Setter method for name.
     * 
     * @param name the name to set
     */
    def public String setName(String name) { this.name = name }

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
    def private writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(this.getName)
        out.writeDouble(this.getLowerBound)
        out.writeDouble(this.getUpperBound)
        out.writeDouble(this.getValue)
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
    def private readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.setName(in.readUTF)
        this.setLowerBound(in.readDouble)
        this.setUpperBound(in.readDouble)
        this.setValue(in.readDouble)
    }

    override int hashCode() {
        this.getLowerBound.hashCode
            .bitwiseXor(this.getUpperBound.hashCode)
            .bitwiseXor(this.getValue.hashCode)
            .bitwiseXor(if(getName === null) 0 else getName.hashCode)
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

    /**
     * Returns a {@link JsonSerializer} and {@link JsonDeserializer} combo class
     * to be used as a {@code TypeAdapter} for this
     * {@code RangedDoubleProperty}.
     * 
     * @return the {@code TypeAdapter} for this class
     */
    def static PropertyTypeAdapter<RangedDoubleProperty> getTypeAdapter() {
        new PropertyTypeAdapter<RangedDoubleProperty>() {
            static val String LOWER_BOUND = "lower bound"
            static val String UPPER_BOUND = "upper bound"

            override deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                val jObj = json as JsonObject

                val name = jObj.get(NAME).asString
                val value = jObj.get(VALUE).asDouble
                val lowerBound = jObj.get(LOWER_BOUND).asDouble
                val upperBound = jObj.get(UPPER_BOUND).asDouble

                new RangedDoubleProperty(name, value, lowerBound, upperBound)
            }

            override serialize(RangedDoubleProperty src, Type typeOfSrc, JsonSerializationContext context) {
                val jObj = new JsonObject

                val name = src.getName
                jObj.addProperty(NAME, name)
                val value = src.getValue
                jObj.addProperty(VALUE, value)
                val lowerBound = src.getLowerBound
                jObj.addProperty(LOWER_BOUND, lowerBound)
                val upperBound = src.getUpperBound
                jObj.addProperty(UPPER_BOUND, upperBound)

                jObj
            }

        }
    }

}
