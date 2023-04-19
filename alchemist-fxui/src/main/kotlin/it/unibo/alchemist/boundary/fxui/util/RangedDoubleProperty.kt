/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.fxui.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import it.unibo.alchemist.boundary.fxui.properties.api.PropertyTypeAdapter
import it.unibo.alchemist.boundary.fxui.properties.api.PropertyTypeAdapter.NAME
import it.unibo.alchemist.boundary.fxui.properties.api.PropertyTypeAdapter.VALUE
import javafx.beans.property.DoublePropertyBase
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.reflect.Type
import kotlin.math.abs

private const val LOWER_BOUND = "lower bound"
private const val UPPER_BOUND = "upper bound"

/**
 * This [DoubleProperty][javafx.beans.property.DoubleProperty] is designed to have a range for the wrapped value
 * and to be serializable.
 *
 * @constructor Based on constructor of [DoubleProperty][DoublePropertyBase], adds the specified bounds.
 * @param name the name of this property
 * @param initialValue the initial value of the wrapped value, defaults to 0.0
 * @param lowerBound the lower bound for the wrapped value to be considered acceptable, defaults to -[Double.MAX_VALUE]
 * @param upperBound the upper bound for the wrapped value to be considered acceptable, defaults to [Double.MAX_VALUE]
 */
class RangedDoubleProperty @JvmOverloads constructor(
    private var name: String,
    initialValue: Double = 0.0,
    lowerBound: Double = -Double.MAX_VALUE,
    upperBound: Double = Double.MAX_VALUE,
) : DoublePropertyBase(initialValue), Serializable {

    /**
     * The lower bound value of the property.
     */
    var lowerBound: Double = lowerBound
        private set

    /**
     * The upper bound value of the property.
     */
    var upperBound: Double = upperBound
        private set

    /**
     * {@inheritDoc}.
     */
    override fun set(newValue: Double) = when {
        newValue < lowerBound -> throw IllegalArgumentException("Provided value is bigger than the upper bound")
        newValue > upperBound -> throw IllegalArgumentException("Provided value is smaller than the lower bound")
        else -> super.set(newValue)
    }

    /**
     * {@inheritDoc}.
     */
    override fun setValue(v: Number) {
        set(v.toDouble())
    }

    /**
     * Getter method for unused field bean.
     *
     * @return null
     */
    override fun getBean() = null

    /**
     * Getter method for name.
     *
     * @return [name]
     */
    override fun getName() = name

    /**
     * Method needed for well working serialization.
     *
     * From [Serializable]:
     * > The [writeObject] method is responsible for writing the state of the
     * object for its particular class so that the corresponding readObject method
     * can restore it. The default mechanism for saving the Object's fields can be
     * invoked by calling [java.io.ObjectOutputStream.defaultWriteObject]. The method does
     * not need to concern itself with the state belonging to its superclasses or
     * subclasses. State is saved by writing the 3 individual fields to the
     * [ObjectOutputStream] using the [writeObject] method or by using the methods
     * for primitive data types supported by [java.io.DataOutput].
     *
     * @param output The output stream
     */
    @Suppress("UnusedPrivateMember")
    private fun writeObject(output: ObjectOutputStream) {
        output.writeUTF(name)
        output.writeDouble(lowerBound)
        output.writeDouble(upperBound)
        output.writeDouble(value)
    }

    /**
     * Method needed for well working serialization.
     *
     * From [Serializable]:
     * > The [readObject] method is
     * responsible for reading from the stream and restoring the classes fields.
     * It may call [java.io.ObjectInputStream.defaultReadObject] to invoke the default mechanism
     * for restoring the object's non-static and non-transient fields. The
     * [java.io.ObjectInputStream.defaultReadObject] method uses information in the stream to assign
     * the fields of the object saved in the stream with the correspondingly
     * named fields in the current object. This handles the case when the class
     * has evolved to add new fields. The method does not need to concern itself
     * with the state belonging to its superclasses or subclasses. State is
     * saved by writing the individual fields to the [ObjectOutputStream]
     * using the [writeObject] method or by using the methods for
     * primitive data types supported by [java.io.DataOutput].
     *
     * @param input The input stream
     */
    @Suppress("UnusedPrivateMember")
    private fun readObject(input: ObjectInputStream) {
        name = input.readUTF()
        lowerBound = input.readDouble()
        upperBound = input.readDouble()
        value = input.readDouble()
    }

    override fun hashCode() =
        lowerBound.hashCode() xor upperBound.hashCode() xor value.hashCode() xor name.hashCode()

    override fun equals(other: Any?) = this === other ||
        javaClass === other?.javaClass &&
        name == (other as RangedDoubleProperty).name &&
        abs(lowerBound - other.lowerBound) < Double.MIN_VALUE &&
        abs(upperBound - other.upperBound) < Double.MIN_VALUE

    companion object {
        private const val serialVersionUID: Long = 1L

        /**
         * Returns a [JsonSerializer][com.google.gson.JsonSerializer] and
         * [JsonDeserializer][com.google.gson.JsonDeserializer] combo class
         * to be used as a [TypeAdapter][com.google.gson.TypeAdapter] for this
         * [RangedDoubleProperty].
         *
         * @return the [TypeAdapter][com.google.gson.TypeAdapter] for this class
         */
        @JvmStatic fun getTypeAdapter(): PropertyTypeAdapter<RangedDoubleProperty> {
            return object :
                PropertyTypeAdapter<RangedDoubleProperty> {
                override fun deserialize(
                    json: JsonElement,
                    typeOfT: Type,
                    context: JsonDeserializationContext,
                ): RangedDoubleProperty {
                    val jObj = json as JsonObject
                    return RangedDoubleProperty(
                        jObj.get(NAME).asString,
                        jObj.get(VALUE).asDouble,
                        jObj.get(LOWER_BOUND).asDouble,
                        jObj.get(UPPER_BOUND).asDouble,
                    )
                }

                override fun serialize(
                    src: RangedDoubleProperty,
                    typeOfSrc: Type,
                    context: JsonSerializationContext,
                ): JsonElement {
                    val jObj = JsonObject()
                    jObj.addProperty(NAME, src.name)
                    jObj.addProperty(VALUE, src.value)
                    jObj.addProperty(LOWER_BOUND, src.lowerBound)
                    jObj.addProperty(UPPER_BOUND, src.upperBound)
                    return jObj
                }
            }
        }
    }
}
